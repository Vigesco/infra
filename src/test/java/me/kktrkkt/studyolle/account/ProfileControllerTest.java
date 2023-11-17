package me.kktrkkt.studyolle.account;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@MockitoSettings
public class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accounts;

    @BeforeEach
    public void beforeEach() {
        if(accounts.findByNickname("user1").isEmpty()){
            Account user1 = new Account();
            user1.setEmail("user1@email.com");
            user1.setNickname("user1");
            user1.setPassword("password1");
            user1.setEmailVerified(true);
            accounts.save(user1);
        }

        if(accounts.findByNickname("user2").isEmpty()) {
            Account user2 = new Account();
            user2.setEmail("user2@email.com");
            user2.setNickname("user2");
            user2.setPassword("password2");
            user2.setEmailVerified(true);
            accounts.save(user2);
        }
    }

    @DisplayName("프로필 조회 테스트 - 주인")
    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "accountUserDetailsService", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void profileView_with_owner() throws Exception {
        this.mockMvc.perform(get("/profile/user1"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile/view"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attribute("isOwner", true))
                .andDo(print());
    }

    @DisplayName("프로필 조회 테스트 - 인증유저")
    @Test
    @WithUserDetails(value = "user2@email.com", userDetailsServiceBeanName = "accountUserDetailsService", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void profileView_with_authenticationUser() throws Exception {
        this.mockMvc.perform(get("/profile/user1"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile/view"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attribute("isOwner", false))
                .andDo(print());
    }
}
