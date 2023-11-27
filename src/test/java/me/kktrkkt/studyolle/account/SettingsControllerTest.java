package me.kktrkkt.studyolle.account;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    @DisplayName("프로필 설정 화면 조회 테스트")
    @Test
    @WithUser1
    void profileUpdateForm() throws Exception {
        this.mockMvc.perform(get(SettingsController.SETTINGS_PROFILE_URL))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingsController.PROFILE_UPDATE_VIEW))
                .andExpect(model().attributeExists("profileUpdateForm"))
                .andDo(print());
    }

    @DisplayName("프로필 설정 처리 테스트 - 성공")
    @Test
    @WithUser1
    void profileUpdateProcess_success() throws Exception {
        String bio = "간략 자기소개";
        String url = "https://github.com/kktrkkt";
        String occupation = "백엔드";
        String location = "대전";

        this.mockMvc.perform(post(SettingsController.SETTINGS_PROFILE_URL)
                        .with(csrf())
                        .param("bio", bio)
                        .param("url", url)
                        .param("occupation", occupation)
                        .param("location", location)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(SettingsController.SETTINGS_PROFILE_URL))
                .andExpect(flash().attributeExists("success"))
                .andDo(print());

        Account user1 = accounts.findByNickname("user1").get();
        Assertions.assertEquals(bio, user1.getBio());
        Assertions.assertEquals(url, user1.getUrl());
        Assertions.assertEquals(occupation, user1.getOccupation());
        Assertions.assertEquals(location, user1.getLocation());
    }

    @DisplayName("프로필 설정 처리 테스트 - 실패")
    @Test
    @WithUser1
    void profileUpdateProcess_failure() throws Exception {
        String over35CharBio = "1234567890123456789012345678901234567890";
        String url = "https://github.com/kktrkkt";
        String occupation = "백엔드";
        String location = "대전";

        this.mockMvc.perform(post(SettingsController.SETTINGS_PROFILE_URL)
                        .with(csrf())
                        .param("bio", over35CharBio)
                        .param("url", url)
                        .param("occupation", occupation)
                        .param("location", location)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingsController.PROFILE_UPDATE_VIEW))
                .andExpect(model().attributeExists("profileUpdateForm"))
                .andExpect(model().hasErrors())
                .andDo(print());

        Account user1 = accounts.findByNickname("user1").get();
        Assertions.assertNotEquals(over35CharBio, user1.getBio());
        Assertions.assertNotEquals(url, user1.getUrl());
        Assertions.assertNotEquals(occupation, user1.getOccupation());
        Assertions.assertNotEquals(location, user1.getLocation());
    }
}
