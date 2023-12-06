package me.kktrkkt.studyolle.account;

import me.kktrkkt.studyolle.account.entity.Account;
import me.kktrkkt.studyolle.infra.MockMvcTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@MockMvcTest
@MockitoSettings
public class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accounts;

    @BeforeEach
    public void beforeEach() {
        createUser("user1@email.com", "user1", "password1");
        createUser("user2@email.com", "user2", "password2");
    }

    @AfterEach
    public void afterEach() {
        accounts.deleteAll();
    }

    private void createUser(String email, String nickname, String password) {
        Account user1 = new Account();
        user1.setEmail(email);
        user1.setNickname(nickname);
        user1.setPassword(password);
        user1.setEmailVerified(true);
        accounts.save(user1);
    }

    @DisplayName("프로필 조회 테스트 - 주인")
    @Test
    @WithUser1
    public void profileView_with_owner() throws Exception {
        this.mockMvc.perform(get(ProfileController.PROFILE_URL+"/user1"))
                .andExpect(status().isOk())
                .andExpect(view().name(ProfileController.PROFILE_VIEW))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attribute("isOwner", true))
                .andDo(print());
    }

    @DisplayName("프로필 조회 테스트 - 인증유저")
    @Test
    @WithUser2
    public void profileView_with_authenticationUser() throws Exception {
        this.mockMvc.perform(get(ProfileController.PROFILE_URL+"/user1"))
                .andExpect(status().isOk())
                .andExpect(view().name(ProfileController.PROFILE_VIEW))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attribute("isOwner", false))
                .andDo(print());
    }
}
