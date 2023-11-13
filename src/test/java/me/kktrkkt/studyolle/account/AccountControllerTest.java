package me.kktrkkt.studyolle.account;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static me.kktrkkt.studyolle.account.Authority.notVerifiedUser;
import static me.kktrkkt.studyolle.account.Authority.user;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AccountControllerTest {

    private static final String KKTRKKT_EMAIL = "kktrkkt@email.com";
    private final String KKTRKKT_NICKNAME = "kktrkkt";
    private final String KKTRKKT_PASSWORD = "password!@#$";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accounts;

    @MockBean
    private JavaMailSender javaMailSender;

    @DisplayName("회원가입 페이지 조회 테스트")
    @Test
    void signUpForm() throws Exception {
        this.mockMvc.perform(get("/sign-up"))
                .andExpect(status().isOk())
                .andExpect(view().name("signUpForm"))
                .andExpect(model().attributeExists("signUpForm"))
                .andDo(print());
    }

    @DisplayName("회원가입 처리 - 성공")
    @Test
    MockHttpSession singUpSubmit_success() throws Exception {
        MockHttpSession session = new MockHttpSession();
        this.mockMvc.perform(post("/sign-up").with(csrf())
                        .param("nickname", KKTRKKT_NICKNAME)
                        .param("email", KKTRKKT_EMAIL)
                        .param("password", KKTRKKT_PASSWORD)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .session(session))
                .andExpect(status().isCreated())
                .andExpect(redirectedUrl("/"))
                .andExpect(authenticated().withAuthorities(List.of(notVerifiedUser())))
                .andDo(print());

        Assertions.assertTrue(accounts.existsByEmail(KKTRKKT_EMAIL));
        then(javaMailSender).should().send(any(SimpleMailMessage.class));
        return session;
    }

    @DisplayName("회원가입 처리 - 이메일 형식 오류")
    @Test
    void singUpSubmit_with_wrong_email() throws Exception {
        String nickname = "kktrkkt";
        String email = "kktrkkt";
        String password = "password!@#$";

        this.mockMvc.perform(post("/sign-up").with(csrf())
                        .param("nickname", nickname)
                        .param("email", email)
                        .param("password", password)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("signUpForm"))
                .andExpect(content().string(containsString("Please provide a valid email address")))
                .andExpect(unauthenticated())
                .andDo(print());
    }

    @DisplayName("회원가입 닉네임 중복 검증 실패 테스트")
    @Test
    void signUpNicknameUnqueFailure() throws Exception {
        String testEmail = "test@email.com";
        Account kktrkkt = Account.builder()
                .nickname(KKTRKKT_NICKNAME)
                .email(testEmail)
                .password(KKTRKKT_PASSWORD)
                .build();

        accounts.save(kktrkkt);

        this.mockMvc.perform(post("/sign-up").with(csrf())
                        .param("nickname", KKTRKKT_NICKNAME)
                        .param("email", KKTRKKT_EMAIL)
                        .param("password", KKTRKKT_PASSWORD)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("signUpForm"))
                .andExpect(content().string(containsString("Nickname is already Existed")))
                .andExpect(unauthenticated())
                .andDo(print());
    }

    @DisplayName("이메일 토큰 검증 테스트 - 성공")
    @Test
    public void verifyEmailToken_success() throws Exception {
        singUpSubmit_success();
        Account kktrkkt = accounts.findByEmail(KKTRKKT_EMAIL).orElse(null);
        assertNotNull(kktrkkt);

        this.mockMvc.perform(get("/check-email-token")
                        .param("token", kktrkkt.getEmailCheckToken())
                        .param("email", kktrkkt.getEmail()))
                .andExpect(status().isOk())
                .andExpect(view().name("checkEmailToken"))
                .andExpect(model().attributeDoesNotExist("error"))
                .andExpect(model().attributeExists("orderByJoinedAt"))
                .andExpect(model().attributeExists("nickname"))
                .andExpect(authenticated().withAuthorities(List.of(user(), notVerifiedUser())))
                .andDo(print());
    }

    @DisplayName("이메일 토큰 검증 테스트 - 실패")
    @Test
    public void verifyEmailToken_faliure() throws Exception {
        singUpSubmit_success();
        Account kktrkkt = accounts.findByEmail(KKTRKKT_EMAIL).orElse(null);
        assertNotNull(kktrkkt);

        this.mockMvc.perform(get("/check-email-token")
                        .param("token", String.valueOf(UUID.randomUUID()))
                        .param("email", kktrkkt.getEmail()))
                .andExpect(status().isOk())
                .andExpect(view().name("checkEmailToken"))
                .andExpect(model().attributeExists("error"))
                .andExpect(unauthenticated())
                .andDo(print());
    }

    /**
     * 이메일 토큰 검증 성공 이후를 기준으로 정식 회원으로 인정하며, 정식 회원을 기준으로 몇번째 가입했는지 정보가 나와야한다
     */
    @DisplayName("이메일 토큰 검증 테스트 - 순서 검증")
    @Test
    public void verifyEmailToken_verifyOrder() throws Exception {
        accounts.save(Account.builder().email("test1@email.com")
                .nickname("nickname1").build());

        singUpSubmit_success();
        Account kktrkkt = accounts.findByEmail(KKTRKKT_EMAIL).orElse(null);
        assertNotNull(kktrkkt);

        this.mockMvc.perform(get("/check-email-token")
                        .param("token", kktrkkt.getEmailCheckToken())
                        .param("email", kktrkkt.getEmail()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("orderByJoinedAt", 1))
                .andDo(print());
    }

    @DisplayName("로그인 페이지 조회 테스트")
    @Test
    public void loginForm() throws Exception {
        this.mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("loginForm"))
                .andDo(print());
    }

    @DisplayName("로그인 처리 테스트 - 성공")
    @Test
    public void loginSubmit_success() throws Exception {
        verifyEmailToken_success();
        this.mockMvc.perform(post("/login")
                        .param("emailOrNickname", KKTRKKT_NICKNAME)
                        .param("password", KKTRKKT_PASSWORD)
                        .param("remember-me", "false")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andDo(print());
    }

    @DisplayName("로그인 처리 테스트 - 실패")
    @Test
    public void loginSubmit_failure() throws Exception {
        verifyEmailToken_success();
        this.mockMvc.perform(post("/login")
                        .param("emailOrNickname", KKTRKKT_NICKNAME)
                        .param("password", "badPassword")
                        .param("remember-me", "false")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"))
                .andDo(print());
    }

    @DisplayName("로그인 자동 로그인 테스트")
    @Test
    public void loginRememberMe() throws Exception {
        verifyEmailToken_success();
        this.mockMvc.perform(post("/login")
                        .param("emailOrNickname", KKTRKKT_NICKNAME)
                        .param("password", KKTRKKT_PASSWORD)
                        .param("remember-me", "true")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(cookie().exists("remember-me"))
                .andDo(print());
    }

    @DisplayName("이메일 검증 전송 페이지 조회 테스트")
    @Test
    public void checkEmailForm() throws Exception {
        MockHttpSession session = singUpSubmit_success();
        this.mockMvc.perform(get("/check-email").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("checkEmailForm"))
                .andDo(print());
    }

    @DisplayName("이메일 검증 전송 테스트")
    @Test
    public void sendValidationEmail() throws Exception {
        MockHttpSession session = singUpSubmit_success();
        this.mockMvc.perform(post("/check-email").session(session).with(csrf())
                        .param("email", KKTRKKT_EMAIL))
                .andExpect(status().isOk())
                .andExpect(view().name("checkEmailForm"))
                .andExpect(model().attributeExists("success"))
                .andDo(print());
        verify(javaMailSender, times(2)).send(any(SimpleMailMessage.class));
    }

    @DisplayName("이메일 검증 전송 테스트 - 6회 전송")
    @Test
    public void sendValidationEmail_6times() throws Exception {
        MockHttpSession session = singUpSubmit_success();

        for(int i=0;i<5;i++){
            this.mockMvc.perform(post("/check-email").session(session).with(csrf())
                            .param("email", KKTRKKT_EMAIL))
                    .andExpect(status().isOk())
                    .andExpect(view().name("checkEmailForm"))
                    .andExpect(model().attributeExists("success"))
                    .andDo(print());
        }

        this.mockMvc.perform(post("/check-email").session(session).with(csrf())
                        .param("email", KKTRKKT_EMAIL))
                .andExpect(status().isOk())
                .andExpect(view().name("checkEmailForm"))
                .andExpect(model().attributeExists("error"))
                .andDo(print());

        verify(javaMailSender, times(6)).send(any(SimpleMailMessage.class));
    }


}