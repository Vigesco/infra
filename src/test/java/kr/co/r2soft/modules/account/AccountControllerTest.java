package kr.co.r2soft.modules.account;

import kr.co.r2soft.modules.account.entity.Account;
import kr.co.r2soft.infra.MockMvcTest;
import kr.co.r2soft.infra.mail.EmailService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static kr.co.r2soft.modules.account.entity.Authority.notVerifiedUser;
import static kr.co.r2soft.modules.account.entity.Authority.user;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
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

@MockMvcTest
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
    private EmailService emailService;

    @DisplayName("회원가입 페이지 조회 테스트")
    @Test
    void signUpForm() throws Exception {
        this.mockMvc.perform(get(AccountController.SIGN_UP_URL))
                .andExpect(status().isOk())
                .andExpect(view().name(AccountController.SIGN_UP_VIEW))
                .andExpect(model().attributeExists("signUpForm"))
                .andDo(print());
    }

    @DisplayName("회원가입 처리 - 성공")
    @Test
    MockHttpSession singUpSubmit_success() throws Exception {
        MockHttpSession session = new MockHttpSession();
        this.mockMvc.perform(post(AccountController.SIGN_UP_URL).with(csrf())
                        .param("nickname", KKTRKKT_NICKNAME)
                        .param("email", KKTRKKT_EMAIL)
                        .param("password", KKTRKKT_PASSWORD)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(authenticated().withAuthorities(List.of(notVerifiedUser())))
                .andDo(print());

        Assertions.assertTrue(accounts.existsByEmail(KKTRKKT_EMAIL));
        then(emailService).should().send(anyString(), anyString(), anyString());
        return session;
    }

    @DisplayName("회원가입 처리 - 이메일 형식 오류")
    @Test
    void singUpSubmit_with_wrong_email() throws Exception {
        String nickname = "kktrkkt";
        String email = "kktrkkt";
        String password = "password!@#$";

        this.mockMvc.perform(post(AccountController.SIGN_UP_URL).with(csrf())
                        .param("nickname", nickname)
                        .param("email", email)
                        .param("password", password)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name(AccountController.SIGN_UP_VIEW))
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

        this.mockMvc.perform(post(AccountController.SIGN_UP_URL).with(csrf())
                        .param("nickname", KKTRKKT_NICKNAME)
                        .param("email", KKTRKKT_EMAIL)
                        .param("password", KKTRKKT_PASSWORD)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name(AccountController.SIGN_UP_VIEW))
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

        this.mockMvc.perform(get(AccountController.CHECK_EMAIL_TOKEN_URL)
                        .param("token", kktrkkt.getEmailCheckToken())
                        .param("email", kktrkkt.getEmail()))
                .andExpect(status().isOk())
                .andExpect(view().name(AccountController.CHECK_EMAIL_TOKEN_VIEW))
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

        this.mockMvc.perform(get(AccountController.CHECK_EMAIL_TOKEN_URL)
                        .param("token", String.valueOf(UUID.randomUUID()))
                        .param("email", kktrkkt.getEmail()))
                .andExpect(status().isOk())
                .andExpect(view().name(AccountController.CHECK_EMAIL_TOKEN_VIEW))
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

        this.mockMvc.perform(get(AccountController.CHECK_EMAIL_TOKEN_URL)
                        .param("token", kktrkkt.getEmailCheckToken())
                        .param("email", kktrkkt.getEmail()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("orderByJoinedAt", 1))
                .andDo(print());
    }

    @DisplayName("로그인 페이지 조회 테스트")
    @Test
    public void loginForm() throws Exception {
        this.mockMvc.perform(get(AccountController.LOGIN_URL))
                .andExpect(status().isOk())
                .andExpect(view().name(AccountController.LOGIN_VIEW))
                .andDo(print());
    }

    @DisplayName("로그인 처리 테스트 - 성공")
    @Test
    public void loginSubmit_success() throws Exception {
        verifyEmailToken_success();
        this.mockMvc.perform(post(AccountController.LOGIN_URL)
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
        this.mockMvc.perform(post(AccountController.LOGIN_URL)
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
        this.mockMvc.perform(post(AccountController.LOGIN_URL)
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
        this.mockMvc.perform(get(AccountController.CHECK_EMAIL_URL).session(session))
                .andExpect(status().isOk())
                .andExpect(view().name(AccountController.CHECK_EMAIL_VIEW))
                .andDo(print());
    }

    @DisplayName("이메일 검증 전송 테스트")
    @Test
    public void sendValidationEmail() throws Exception {
        MockHttpSession session = singUpSubmit_success();
        this.mockMvc.perform(post(AccountController.CHECK_EMAIL_URL).session(session).with(csrf())
                        .param("email", KKTRKKT_EMAIL))
                .andExpect(status().isOk())
                .andExpect(view().name(AccountController.CHECK_EMAIL_VIEW))
                .andExpect(model().attributeExists("success"))
                .andDo(print());
        verify(emailService, times(2)).send(anyString(), anyString(), anyString());
    }

    @DisplayName("이메일 검증 전송 테스트 - 6회 전송")
    @Test
    public void sendValidationEmail_6times() throws Exception {
        MockHttpSession session = singUpSubmit_success();

        for(int i=0;i<5;i++){
            this.mockMvc.perform(post(AccountController.CHECK_EMAIL_URL).session(session).with(csrf())
                            .param("email", KKTRKKT_EMAIL))
                    .andExpect(status().isOk())
                    .andExpect(view().name(AccountController.CHECK_EMAIL_VIEW))
                    .andExpect(model().attributeExists("success"))
                    .andDo(print());
        }

        this.mockMvc.perform(post(AccountController.CHECK_EMAIL_URL).session(session).with(csrf())
                        .param("email", KKTRKKT_EMAIL))
                .andExpect(status().isOk())
                .andExpect(view().name(AccountController.CHECK_EMAIL_VIEW))
                .andExpect(model().attributeExists("error"))
                .andDo(print());

        verify(emailService, times(6)).send(anyString(), anyString(), anyString());
    }

    @DisplayName("패스워드 없이 로그인하기 페이지 조회 테스트")
    @Test
    public void loginWithoutPasswordForm() throws Exception {
        this.mockMvc.perform(get(AccountController.LOGIN_WITHOUT_PASSWORD_URL))
                .andExpect(status().isOk())
                .andExpect(view().name(AccountController.LOGIN_WITHOUT_PASSWORD_VIEW))
                .andDo(print());
    }

    @DisplayName("로그인 링크 이메일 전송 테스트 - 성공")
    @Test
    public void sendLoginLinkEmail_success() throws Exception {
        singUpSubmit_success();
        this.mockMvc.perform(post(AccountController.LOGIN_WITHOUT_PASSWORD_URL)
                        .with(csrf())
                        .param("email", KKTRKKT_EMAIL))
                .andExpect(status().isOk())
                .andExpect(view().name(AccountController.LOGIN_WITHOUT_PASSWORD_VIEW))
                .andExpect(model().attributeExists("success"))
                .andDo(print());
        verify(emailService, times(2)).send(anyString(), anyString(), anyString());
    }

    @DisplayName("로그인 링크 이메일 전송 테스트 - 6회 전송")
    @Test
    public void sendLoginLinkEmail_6times() throws Exception {
        singUpSubmit_success();
        for (int i = 0; i < 5; i++) {
            this.mockMvc.perform(post(AccountController.LOGIN_WITHOUT_PASSWORD_URL)
                            .with(csrf())
                            .param("email", KKTRKKT_EMAIL))
                    .andExpect(status().isOk())
                    .andExpect(view().name(AccountController.LOGIN_WITHOUT_PASSWORD_VIEW))
                    .andExpect(model().attributeExists("success"))
                    .andDo(print());
        }

        this.mockMvc.perform(post(AccountController.LOGIN_WITHOUT_PASSWORD_URL)
                        .with(csrf())
                        .param("email", KKTRKKT_EMAIL))
                .andExpect(status().isOk())
                .andExpect(view().name(AccountController.LOGIN_WITHOUT_PASSWORD_VIEW))
                .andExpect(model().attributeExists("error"))
                .andDo(print());

        verify(emailService, times(6)).send(anyString(), anyString(), anyString());
    }

    @DisplayName("이메일 로그인 - 성공")
    @Test
    public void loginByEmail_success() throws Exception {
        singUpSubmit_success();
        Account kktrkkt = accounts.findByEmail(KKTRKKT_EMAIL).get();

        this.mockMvc.perform(get(AccountController.LOGIN_BY_EMAIL_URL)
                        .param("email", KKTRKKT_EMAIL)
                        .param("token", kktrkkt.getEmailCheckToken()))
                .andExpect(status().isOk())
                .andExpect(view().name(AccountController.LOGIN_BY_EMAIL_VIEW))
                .andExpect(model().attributeDoesNotExist("error"))
                .andDo(print());
    }

    @DisplayName("이메일 로그인 - 실패")
    @Test
    public void loginByEmail_failure() throws Exception {
        singUpSubmit_success();
        Account kktrkkt = accounts.findByEmail(KKTRKKT_EMAIL).get();

        this.mockMvc.perform(get(AccountController.LOGIN_BY_EMAIL_URL)
                        .param("email", "wrongEmail")
                        .param("token", kktrkkt.getEmailCheckToken()))
                .andExpect(status().isOk())
                .andExpect(view().name(AccountController.LOGIN_BY_EMAIL_VIEW))
                .andExpect(model().attributeExists("error"))
                .andDo(print());

        this.mockMvc.perform(get(AccountController.LOGIN_BY_EMAIL_URL)
                        .param("email", KKTRKKT_EMAIL)
                        .param("token", "wrongToken"))
                .andExpect(status().isOk())
                .andExpect(view().name(AccountController.LOGIN_BY_EMAIL_VIEW))
                .andExpect(model().attributeExists("error"))
                .andDo(print());
    }
}
