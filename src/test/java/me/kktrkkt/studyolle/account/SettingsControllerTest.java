package me.kktrkkt.studyolle.account;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class SettingsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accounts;

    @BeforeEach
    public void beforeEach() {
        if(accounts.findByNickname("user1").isEmpty()){
            createUser("user1@email.com", "user1", "password1");
        }
        if(accounts.findByNickname("user2").isEmpty()) {
            createUser("user2@email.com", "user2", "password2");
        }
    }

    private void createUser(String email, String nickname, String password) {
        Account user1 = new Account();
        user1.setEmail(email);
        user1.setNickname(nickname);
        user1.setPassword(password);
        user1.setEmailVerified(true);
        accounts.save(user1);
    }

    @DisplayName("프로필 설정 화면 조회 테스트")
    @Test
    @WithUser1
    void profileUpdateForm() throws Exception {
        this.mockMvc.perform(get("/settings/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("settings/profileUpdateForm"))
                .andExpect(model().attributeExists("profileUpdateForm"))
                .andDo(print());
    }
}
