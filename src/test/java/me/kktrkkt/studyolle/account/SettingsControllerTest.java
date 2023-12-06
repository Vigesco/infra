package me.kktrkkt.studyolle.account;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.kktrkkt.studyolle.account.entity.Account;
import me.kktrkkt.studyolle.infra.MockMvcTest;
import me.kktrkkt.studyolle.topic.Topic;
import me.kktrkkt.studyolle.topic.TopicForm;
import me.kktrkkt.studyolle.topic.TopicRepository;
import me.kktrkkt.studyolle.zone.Zone;
import me.kktrkkt.studyolle.zone.ZoneForm;
import me.kktrkkt.studyolle.zone.ZoneRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@MockMvcTest
@Transactional
public class SettingsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accounts;

    @Autowired
    private TopicRepository topics;

    @Autowired
    private ZoneRepository zones;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @DisplayName("프로필 설정 화면 조회 테스트")
    @Test
    @WithAccount("user1")
    void profileUpdateForm() throws Exception {
        this.mockMvc.perform(get(SettingsController.SETTINGS_PROFILE_URL))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingsController.PROFILE_UPDATE_VIEW))
                .andExpect(model().attributeExists("profileUpdateForm"))
                .andDo(print());
    }

    @DisplayName("프로필 설정 처리 테스트 - 성공")
    @Test
    @WithAccount("user1")
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
    @WithAccount("user1")
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

    @DisplayName("패스워드 설정 화면 조회 테스트")
    @Test
    @WithAccount("user1")
    void passwordUpdateForm() throws Exception {
        this.mockMvc.perform(get(SettingsController.SETTINGS_PASSWORD_URL))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingsController.PASSWORD_UPDATE_VIEW))
                .andExpect(model().attributeExists("passwordUpdateForm"))
                .andDo(print());
    }

    @DisplayName("패스워드 설정 처리 테스트 - 성공")
    @Test
    @WithAccount("user1")
    void passwordUpdateProcess_success() throws Exception {
        String password = "asdF!@#$";

        this.mockMvc.perform(post(SettingsController.SETTINGS_PASSWORD_URL)
                        .with(csrf())
                        .param("password", password)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(SettingsController.SETTINGS_PASSWORD_URL))
                .andExpect(flash().attributeExists("success"))
                .andDo(print());

        Account user1 = accounts.findByNickname("user1").get();
        Assertions.assertTrue(passwordEncoder.matches(password, user1.getPassword()));
    }

    @DisplayName("패스워드 설정 처리 테스트 - 실패")
    @Test
    @WithAccount("user1")
    void passwordUpdateProcess_failure() throws Exception {
        Account user1 = accounts.findByNickname("user1").get();
        String oldPassword = user1.getPassword();
        String password = "1234567";

        this.mockMvc.perform(post(SettingsController.SETTINGS_PASSWORD_URL)
                        .with(csrf())
                        .param("password", password)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingsController.PASSWORD_UPDATE_VIEW))
                .andExpect(model().attributeExists("passwordUpdateForm"))
                .andExpect(model().hasErrors())
                .andDo(print());

        user1 = accounts.findById(user1.getId()).get();
        Assertions.assertEquals(oldPassword, user1.getPassword());
    }

    @DisplayName("알림 설정 화면 조회 테스트")
    @Test
    @WithAccount("user1")
    void notificationUpdateForm() throws Exception {
        this.mockMvc.perform(get(SettingsController.SETTINGS_NOTIFICATION_URL))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingsController.NOTIFICATION_UPDATE_VIEW))
                .andExpect(model().attributeExists("notificationUpdateForm"))
                .andDo(print());
    }

    @DisplayName("알림 설정 저장 테스트")
    @Test
    @WithAccount("user1")
    void notificationSave_success() throws Exception {
        String studyCreatedByEmail = "true";
        String studyCreatedByWeb = "true";
        String studyEnrollmentResultByEmail = "false";
        String studyEnrollmentResultByWeb = "true";
        String studyUpdatedByEmail = "false";
        String studyUpdatedByWeb = "false";

        this.mockMvc.perform(post(SettingsController.SETTINGS_NOTIFICATION_URL)
                        .with(csrf())
                        .param("studyCreatedByEmail", studyCreatedByEmail)
                        .param("studyCreatedByWeb", studyCreatedByWeb)
                        .param("studyEnrollmentResultByEmail", studyEnrollmentResultByEmail)
                        .param("studyEnrollmentResultByWeb", studyEnrollmentResultByWeb)
                        .param("studyUpdatedByEmail", studyUpdatedByEmail)
                        .param("studyUpdatedByWeb", studyUpdatedByWeb)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(SettingsController.SETTINGS_NOTIFICATION_URL))
                .andExpect(flash().attributeExists("success"))
                .andDo(print());

        Account user1 = accounts.findByNickname("user1").get();
        Assertions.assertEquals(Boolean.valueOf(studyCreatedByEmail), user1.isStudyCreatedByEmail());
        Assertions.assertEquals(Boolean.valueOf(studyCreatedByWeb), user1.isStudyCreatedByWeb());
        Assertions.assertEquals(Boolean.valueOf(studyEnrollmentResultByEmail), user1.isStudyEnrollmentResultByEmail());
        Assertions.assertEquals(Boolean.valueOf(studyEnrollmentResultByWeb), user1.isStudyEnrollmentResultByWeb());
        Assertions.assertEquals(Boolean.valueOf(studyUpdatedByEmail), user1.isStudyUpdatedByEmail());
        Assertions.assertEquals(Boolean.valueOf(studyUpdatedByWeb), user1.isStudyUpdatedByWeb());
    }

    @DisplayName("관심주제 설정 화면 조회 테스트")
    @Test
    @WithAccount("user1")
    void topicUpdateForm() throws Exception {
        this.mockMvc.perform(get(SettingsController.SETTINGS_TOPIC_URL))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingsController.TOPIC_UPDATE_VIEW))
                .andExpect(model().attributeExists("topicList"))
                .andExpect(model().attributeExists("whiteList"))
                .andDo(print());
    }

    @DisplayName("관심주제 추가 - 성공")
    @Test
    @WithAccount("user1")
    void addTopic_success() throws Exception {
        String spring = "스프링";
        requestTopic(spring, "/add", status().isOk());

        Optional<Topic> springTopic = topics.findByTitle(spring);
        Assertions.assertTrue(springTopic.isPresent());
        Account user1 = accounts.findByNickname("user1").get();
        Assertions.assertTrue(user1.getTopics().contains(springTopic.get()));
    }

    @DisplayName("관심주제 중복 추가 - 성공")
    @Test
    @WithAccount("user1")
    void addDuplicationTopic_success() throws Exception {
        Topic topic = new Topic();
        String title = "스프링";
        topic.setTitle(title);
        topics.save(topic);

        requestTopic(title, "/add", status().isOk());

        List<Topic> topicAll  = topics.findAll();
        Assertions.assertEquals(1, topicAll.stream().filter(x -> x.getTitle().equals(title)).count());
    }

    @DisplayName("관심주제 추가 - 실패")
    @Test
    @WithAccount("user1")
    void addTopic_failure() throws Exception {
        requestTopic("스", "/remove", status().isBadRequest());
        requestTopic("1", "/remove", status().isBadRequest());
        requestTopic("스 프 링", "/remove", status().isBadRequest());
        requestTopic("가나다라마가나다라마가나다라마가나다라마가", "/remove", status().isBadRequest());
        requestTopic("ㄱ", "/remove", status().isBadRequest());

        Account user1 = accounts.findByNickname("user1").get();
        Assertions.assertTrue(user1.getTopics().isEmpty());
    }


    @DisplayName("관심주제 삭제 - 성공")
    @Test
    @WithAccount("user1")
    void removeTopic_success() throws Exception {
        String spring = "스프링";
        requestTopic(spring, "/add", status().isOk());
        requestTopic(spring, "/remove", status().isOk());

        Optional<Topic> springTopic = topics.findByTitle(spring);
        Assertions.assertTrue(springTopic.isPresent());
        Account user1 = accounts.findByNickname("user1").get();
        Assertions.assertTrue(user1.getTopics().isEmpty());
    }

    @DisplayName("관심주제 삭제 - 실패")
    @Test
    @WithAccount("user1")
    void removeTopic_failure() throws Exception {
        String spring = "스프링";
        requestTopic(spring, "/remove", status().isBadRequest());

        Optional<Topic> springTopic = topics.findByTitle(spring);
        Assertions.assertTrue(springTopic.isEmpty());
        Account user1 = accounts.findByNickname("user1").get();
        Assertions.assertTrue(user1.getTopics().isEmpty());
    }

    private void requestTopic(String title, String url, ResultMatcher status) throws Exception {
        this.mockMvc.perform(post(SettingsController.SETTINGS_TOPIC_URL + url)
                        .with(csrf())
                        .content(objectMapper.writeValueAsString(topicUpdateForm(title)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status)
                .andDo(print());
    }

    private TopicForm topicUpdateForm(String title) {
        TopicForm topicUpdateForm = new TopicForm();
        topicUpdateForm.setTitle(title);
        return topicUpdateForm;
    }

    @DisplayName("주요 지역 설정 화면 조회 테스트")
    @Test
    @WithAccount("user1")
    void zoneUpdateForm() throws Exception {
        this.mockMvc.perform(get(SettingsController.SETTINGS_ZONE_URL))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingsController.ZONE_UPDATE_VIEW))
                .andExpect(model().attributeExists("zoneList"))
                .andExpect(model().attributeExists("whiteList"))
                .andDo(print());
    }

    @DisplayName("주요지역 추가 - 성공")
    @Test
    @WithAccount("user1")
    void addZone_success() throws Exception {
        zones.save(Zone.builder().city("test").localNameOfCity("테스트").province("testp").build());
        Optional<Zone> testZone = zones.findByCityAndProvince("test", "testp");

        Assertions.assertTrue(testZone.isPresent());
        requestZone(testZone.get().toString(), "/add", status().isOk());
        Account user1 = accounts.findByNickname("user1").get();
        Assertions.assertTrue(user1.getZones().contains(testZone.get()));
    }

    @DisplayName("주요지역 추가 - 실패")
    @Test
    @WithAccount("user1")
    void addZone_failure() throws Exception {
        Zone wrongCity = zones.findByCityAndProvince("wrongCity", "유토피아").orElse(null);

        Assertions.assertNull(wrongCity);
        requestZone("wrong(Zone)/Name", "/add", status().isBadRequest());
        Account user1 = accounts.findByNickname("user1").get();
        Assertions.assertTrue(user1.getZones().isEmpty());
    }

    @DisplayName("주요지역 삭제 - 성공")
    @Test
    @WithAccount("user1")
    void removeZone_success() throws Exception {
        zones.save(Zone.builder().city("test").localNameOfCity("테스트").province("testp").build());
        Optional<Zone> testZone = zones.findByCityAndProvince("test", "testp");

        Assertions.assertTrue(testZone.isPresent());
        requestZone(testZone.get().toString(), "/add", status().isOk());
        requestZone(testZone.get().toString(), "/remove", status().isOk());

        Account user1 = accounts.findByNickname("user1").get();
        Assertions.assertTrue(user1.getZones().isEmpty());
    }

    @DisplayName("주요지역 삭제 - 실패")
    @Test
    @WithAccount("user1")
    void removeZone_failure() throws Exception {
        Zone wrongCity = zones.findByCityAndProvince("wrongCity", "유토피아").orElse(null);

        Assertions.assertNull(wrongCity);
        requestZone("wrong(Zone)/Name", "/remove", status().isBadRequest());

        Account user1 = accounts.findByNickname("user1").get();
        Assertions.assertTrue(user1.getZones().isEmpty());
    }

    private void requestZone(String zoneName, String url, ResultMatcher status) throws Exception {
        this.mockMvc.perform(post(SettingsController.SETTINGS_ZONE_URL + url)
                        .with(csrf())
                        .content(objectMapper.writeValueAsString(zoneForm(zoneName)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status)
                .andDo(print());
    }

    private ZoneForm zoneForm(String zoneName) {
        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneName(zoneName);
        return zoneForm;
    }

    @DisplayName("계정 설정 화면 조회 테스트")
    @Test
    @WithAccount("user1")
    void accountUpdateForm() throws Exception {
        this.mockMvc.perform(get(SettingsController.SETTINGS_ACCOUNT_URL))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingsController.ACCOUNT_UPDATE_VIEW))
                .andExpect(model().attributeExists("nicknameUpdateForm"))
                .andDo(print());
    }

    @DisplayName("닉네임 변경 처리 테스트 - 성공")
    @Test
    @WithAccount("user1")
    void nicknameUpdateProcess_success() throws Exception {
        String nickname = "newUser";

        this.mockMvc.perform(post(SettingsController.SETTINGS_NICKNAME_URL)
                        .with(csrf())
                        .param("nickname", nickname)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(SettingsController.SETTINGS_ACCOUNT_URL))
                .andExpect(flash().attributeExists("success"))
                .andDo(print());

        Optional<Account> newUser = accounts.findByNickname("newUser");
        Assertions.assertTrue(newUser.isPresent());
    }

    @DisplayName("닉네임 변경 처리 테스트 - 실패")
    @Test
    @WithAccount({"user1", "user2"})
    void nicknameUpdateProcess_failure() throws Exception {
        updateNickname("user2");
        updateNickname("us");
        updateNickname("");

        Optional<Account> user1 = accounts.findByNickname("user1");
        Assertions.assertTrue(user1.isPresent());
    }

    private void updateNickname(String nickname1) throws Exception {
        this.mockMvc.perform(post(SettingsController.SETTINGS_NICKNAME_URL)
                        .with(csrf())
                        .param("nickname", nickname1)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingsController.ACCOUNT_UPDATE_VIEW))
                .andExpect(model().attributeExists("nicknameUpdateForm"))
                .andExpect(model().hasErrors())
                .andDo(print());
    }
}
