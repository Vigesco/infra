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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AccountControllerTest {

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
    void singUpSubmit_success() throws Exception {
        String nickname = "kktrkkt";
        String email = "kktrkkt@email.com";
        String password = "password!@#$";

        this.mockMvc.perform(post("/sign-up").with(csrf())
                        .param("nickname", nickname)
                        .param("email", email)
                        .param("password", password)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isCreated())
                .andExpect(redirectedUrl("/"))
                .andDo(print());

        Assertions.assertTrue(accounts.existsByEmail(email));
        then(javaMailSender).should().send(any(SimpleMailMessage.class));
    }

    @DisplayName("회원가입 이메일 검증 실패 테스트")
    @Test
    void signUpEmailFailure() throws Exception {
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
                .andDo(print());
    }

    @DisplayName("회원가입 닉네임 중복 검증 실패 테스트")
    @Test
    void signUpNicknameUnqueFailure() throws Exception {
        Account kktrkkt = Account.builder()
                .nickname("kktrkkt")
                .email("test@email.com")
                .password("password!@#$")
                .build();

        accounts.save(kktrkkt);

        String nickname = "kktrkkt";
        String email = "kktrkkt@email.com";
        String password = "password!@#$";

        this.mockMvc.perform(post("/sign-up").with(csrf())
                        .param("nickname", nickname)
                        .param("email", email)
                        .param("password", password)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("signUpForm"))
                .andExpect(content().string(containsString("Nickname is already Existed")))
                .andDo(print());
    }
}