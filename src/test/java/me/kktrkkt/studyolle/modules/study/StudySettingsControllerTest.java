package me.kktrkkt.studyolle.modules.study;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.kktrkkt.studyolle.infra.MockMvcTest;
import me.kktrkkt.studyolle.modules.account.AccountRepository;
import me.kktrkkt.studyolle.modules.account.WithAccount;
import me.kktrkkt.studyolle.modules.account.entity.Account;
import me.kktrkkt.studyolle.modules.study.event.StudyCreatedEvent;
import me.kktrkkt.studyolle.modules.study.event.StudyEventListener;
import me.kktrkkt.studyolle.modules.study.event.StudyUpdatedEvent;
import me.kktrkkt.studyolle.modules.topic.Topic;
import me.kktrkkt.studyolle.modules.topic.TopicForm;
import me.kktrkkt.studyolle.modules.topic.TopicRepository;
import me.kktrkkt.studyolle.modules.zone.Zone;
import me.kktrkkt.studyolle.modules.zone.ZoneForm;
import me.kktrkkt.studyolle.modules.zone.ZoneRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import static me.kktrkkt.studyolle.infra.Utils.replacePath;
import static me.kktrkkt.studyolle.modules.study.StudySettingsController.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@MockMvcTest
@Transactional
class StudySettingsControllerTest{

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StudyFactory studyFactory;

    @Autowired
    private StudyRepository studys;

    @Autowired
    private AccountRepository accounts;

    @Autowired
    private TopicRepository topics;

    @Autowired
    private ZoneRepository zones;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StudyEventListener studyEventListener;

    @DisplayName("스터디 설정 소개 조회")
    @Test
    @WithAccount("user1")
    void studySettingsInfoForm() throws Exception {
        Study study = studyFactory.createStudy("user1");

        this.mockMvc.perform(get(replacePath(study.getPath(), SETTINGS_INFO_URL)))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("study"))
                .andExpect(model().attributeExists("studyInfoForm"))
                .andExpect(view().name(SETTINGS_INFO_VIEW))
                .andDo(print());
    }

    @DisplayName("스터디 소개 설정 - 성공")
    @Test
    @WithAccount("user1")
    void updateStudyInfo_success() throws Exception {
        Study study = studyFactory.createStudy("user1");
        String studySettingsInfoUrl = replacePath(study.getPath(), SETTINGS_INFO_URL);

        String bio = "new-bio";
        String explanation = "new-explanation";
        this.mockMvc.perform(post(studySettingsInfoUrl)
                        .with(csrf())
                        .param("bio", bio)
                        .param("explanation", explanation)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(studySettingsInfoUrl))
                .andExpect(flash().attributeExists("success"))
                .andDo(print());
        Study byId = studys.findById(study.getId()).get();
        assertEquals(bio, byId.getBio());
        assertEquals(explanation, byId.getExplanation());

        then(studyEventListener).should().handleStudyUpdatedEvent(any(StudyUpdatedEvent.class));
    }

    @DisplayName("스터디 소개 설정 - 실패")
    @Test
    @WithAccount("user1")
    void updateStudyInfo_failure() throws Exception {
        Study study = studyFactory.createStudy("user1");
        String studySettingsInfoUrl = replacePath(study.getPath(), SETTINGS_INFO_URL);

        String over256 = new Random().ints(0, 1)
                .limit(256)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

        String bio = over256;
        String explanation = "new-explanation";
        this.mockMvc.perform(post(studySettingsInfoUrl)
                        .with(csrf())
                        .param("bio", bio)
                        .param("explanation", explanation)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(view().name(SETTINGS_INFO_VIEW))
                .andExpect(model().attributeExists("study"))
                .andExpect(model().attributeExists("studyInfoForm"))
                .andExpect(model().hasErrors())
                .andDo(print());
    }

    @DisplayName("스터디 설정 배너 조회")
    @Test
    @WithAccount("user1")
    void studySettingsBannerForm() throws Exception {
        Study study = studyFactory.createStudy("user1");

        this.mockMvc.perform(get(replacePath(study.getPath(), SETTINGS_BANNER_URL)))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("study"))
                .andExpect(view().name(SETTINGS_BANNER_VIEW))
                .andDo(print());
    }

    @DisplayName("스터디 배너 사용 설정")
    @Test
    @WithAccount("user1")
    void updateStudyBannerUse_success() throws Exception {
        Study study = studyFactory.createStudy("user1");
        String studySettingsBannerUrl = replacePath(study.getPath(), SETTINGS_BANNER_URL);

        this.mockMvc.perform(post(studySettingsBannerUrl+"/true").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(studySettingsBannerUrl))
                .andExpect(flash().attributeExists("success"))
                .andDo(print());
        Study byId = studys.findById(study.getId()).get();
        assertTrue(byId.isUseBanner());

        this.mockMvc.perform(post(studySettingsBannerUrl+"/false").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(studySettingsBannerUrl))
                .andExpect(flash().attributeExists("success"))
                .andDo(print());
        assertFalse(byId.isUseBanner());
    }

    @DisplayName("스터디 배너 설정")
    @Test
    @WithAccount("user1")
    void updateStudyBanner_success() throws Exception {
        Study study = studyFactory.createStudy("user1");
        String studySettingsBannerUrl = replacePath(study.getPath(), SETTINGS_BANNER_URL);

        String banner = "banner";
        this.mockMvc.perform(post(studySettingsBannerUrl)
                        .with(csrf())
                        .param("banner", banner)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(studySettingsBannerUrl))
                .andExpect(flash().attributeExists("success"))
                .andDo(print());
        Study byId = studys.findById(study.getId()).get();
        assertEquals(banner, byId.getBanner());
    }

    @DisplayName("스터디 설정 관심주제 조회")
    @Test
    @WithAccount("user1")
    void studySettingsTopicForm() throws Exception {
        Study study = studyFactory.createStudy("user1");

        this.mockMvc.perform(get(replacePath(study.getPath(), SETTINGS_TOPIC_URL)))
                .andExpect(status().isOk())
                .andExpect(view().name(SETTINGS_TOPIC_VIEW))
                .andExpect(model().attributeExists("topicList"))
                .andExpect(model().attributeExists("whiteList"))
                .andDo(print());
    }

    @DisplayName("스터디 관심주제 추가 - 성공")
    @Test
    @WithAccount("user1")
    void addStudyTopic_success() throws Exception {
        Study study = studyFactory.createStudy("user1");
        String spring = "스프링";
        requestTopic(spring, replacePath(study.getPath(), SETTINGS_TOPIC_URL) + "/add", status().isOk());

        Optional<Topic> springTopic = topics.findByTitle(spring);
        Assertions.assertTrue(springTopic.isPresent());
        Assertions.assertTrue(study.getTopics().contains(springTopic.get()));
    }

    @DisplayName("스터디 관심주제 중복 추가 - 성공")
    @Test
    @WithAccount("user1")
    void addDuplicationStudyTopic_success() throws Exception {
        Study study = studyFactory.createStudy("user1");
        Topic topic = new Topic();
        String title = "스프링";
        topic.setTitle(title);
        topics.save(topic);

        requestTopic(title, replacePath(study.getPath(), SETTINGS_TOPIC_URL) + "/add", status().isOk());

        List<Topic> topicAll  = topics.findAll();
        Assertions.assertEquals(1, topicAll.stream().filter(x -> x.getTitle().equals(title)).count());
    }

    @DisplayName("스터디 관심주제 추가 - 실패")
    @Test
    @WithAccount("user1")
    void addStudyTopic_failure() throws Exception {
        Study study = studyFactory.createStudy("user1");

        requestTopic("스", replacePath(study.getPath(), SETTINGS_TOPIC_URL) + "/add", status().isBadRequest());
        requestTopic("1", replacePath(study.getPath(), SETTINGS_TOPIC_URL) + "/add", status().isBadRequest());
        requestTopic("스 프 링", replacePath(study.getPath(), SETTINGS_TOPIC_URL) + "/add", status().isBadRequest());
        requestTopic("가나다라마가나다라마가나다라마가나다라마가", replacePath(study.getPath(), SETTINGS_TOPIC_URL) + "/add", status().isBadRequest());
        requestTopic("ㄱ", replacePath(study.getPath(), SETTINGS_TOPIC_URL) + "/add", status().isBadRequest());

        Assertions.assertTrue(study.getTopics().isEmpty());
    }


    @DisplayName("스터디 관심주제 삭제 - 성공")
    @Test
    @WithAccount("user1")
    void removeStudyTopic_success() throws Exception {
        Study study = studyFactory.createStudy("user1");

        String spring = "스프링";
        requestTopic(spring, replacePath(study.getPath(), SETTINGS_TOPIC_URL) + "/add", status().isOk());
        requestTopic(spring, replacePath(study.getPath(), SETTINGS_TOPIC_URL) + "/remove", status().isOk());

        Optional<Topic> springTopic = topics.findByTitle(spring);
        Assertions.assertTrue(springTopic.isPresent());
        Assertions.assertTrue(study.getTopics().isEmpty());
    }

    @DisplayName("스터디 관심주제 삭제 - 실패")
    @Test
    @WithAccount("user1")
    void removeStudyTopic_failure() throws Exception {
        Study study = studyFactory.createStudy("user1");

        String spring = "스프링";
        requestTopic(spring, replacePath(study.getPath(), SETTINGS_TOPIC_URL) + "/remove", status().isBadRequest());

        Optional<Topic> springTopic = topics.findByTitle(spring);
        Assertions.assertTrue(springTopic.isEmpty());
        Assertions.assertTrue(study.getTopics().isEmpty());
    }

    @DisplayName("스터디 설정 활동지역 조회")
    @Test
    @WithAccount("user1")
    void studySettingsZoneForm() throws Exception {
        Study study = studyFactory.createStudy("user1");

        this.mockMvc.perform(get(replacePath(study.getPath(), SETTINGS_ZONE_URL)))
                .andExpect(status().isOk())
                .andExpect(view().name(SETTINGS_ZONE_VIEW))
                .andExpect(model().attributeExists("zoneList"))
                .andExpect(model().attributeExists("whiteList"))
                .andDo(print());
    }

    @DisplayName("스터디 주요지역 추가 - 성공")
    @Test
    @WithAccount("user1")
    void addStudyZone_success() throws Exception {
        Study study = studyFactory.createStudy("user1");
        Zone testZone = Zone.builder().city("test").localNameOfCity("테스트").province("testp").build();
        zones.save(testZone);

        requestZone(testZone.toString(), replacePath(study.getPath(), SETTINGS_ZONE_URL) +"/add", status().isOk());

        Assertions.assertTrue(study.getZones().contains(testZone));
    }

    @DisplayName("스터디 주요지역 추가 - 실패")
    @Test
    @WithAccount("user1")
    void addStudyZone_failure() throws Exception {
        Study study = studyFactory.createStudy("user1");

        requestZone("wrong(Zone)/Name", replacePath(study.getPath(), SETTINGS_ZONE_URL) +"/add", status().isBadRequest());

        Assertions.assertTrue(study.getZones().isEmpty());
    }

    @DisplayName("스터디 주요지역 삭제 - 성공")
    @Test
    @WithAccount("user1")
    void
    _success() throws Exception {
        Study study = studyFactory.createStudy("user1");

        Zone testZone = Zone.builder().city("test").localNameOfCity("테스트").province("testp").build();
        zones.save(testZone);

        requestZone(testZone.toString(), replacePath(study.getPath(), SETTINGS_ZONE_URL) +"/add", status().isOk());
        requestZone(testZone.toString(), replacePath(study.getPath(), SETTINGS_ZONE_URL) +"/remove", status().isOk());

        Assertions.assertTrue(zones.findById(testZone.getId()).isPresent());
        Assertions.assertTrue(study.getZones().isEmpty());
    }

    @DisplayName("스터디 주요지역 삭제 - 실패")
    @Test
    @WithAccount("user1")
    void removeStudyZone_failure() throws Exception {
        Study study = studyFactory.createStudy("user1");

        requestZone("wrong(Zone)/Name", replacePath(study.getPath(), SETTINGS_ZONE_URL) +"/remove", status().isBadRequest());

        Assertions.assertTrue(study.getZones().isEmpty());
    }

    @DisplayName("스터디 설정 스터디 조회")
    @Test
    @WithAccount("user1")
    void studySettingsStudyForm() throws Exception {
        Study study = studyFactory.createStudy("user1");
        this.mockMvc.perform(get(replacePath(study.getPath(), SETTINGS_STUDY_URL)))
                .andExpect(status().isOk())
                .andExpect(view().name(SETTINGS_STUDY_VIEW))
                .andExpect(model().attributeExists("studyPathForm"))
                .andExpect(model().attributeExists("studyTitleForm"))
                .andDo(print());
    }

    @DisplayName("스터디 공개 - 성공")
    @Test
    @WithAccount({"user1", "user2", "user3", "user4", "user5"})
    void publishStudy_success() throws Exception {
        Study study = studyFactory.createStudy("user1");
        Zone city1 = Zone.builder().city("city1").localNameOfCity("name1").province("province1").build();
        Topic spring = Topic.builder().title("spring").build();
        Topic java = Topic.builder().title("java").build();
        Zone city2 = Zone.builder().city("city2").localNameOfCity("name2").province("province2").build();
        zones.save(city1);
        topics.save(spring);
        zones.save(city2);
        topics.save(java);

        study.getZones().add(city1);
        study.getTopics().add(spring);
        Account user2 = accounts.findByNickname("user2").get();
        Account user3 = accounts.findByNickname("user3").get();
        Account user4 = accounts.findByNickname("user4").get();

        user2.getTopics().add(spring);
        user3.getZones().add(city1);
        user4.getTopics().add(java);
        user4.getZones().add(city2);

        String settingsStudyPublishedUrl = replacePath(study.getPath(), SETTINGS_STUDY_URL);

        this.mockMvc.perform(post(settingsStudyPublishedUrl +"/publish").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(settingsStudyPublishedUrl))
                .andExpect(flash().attribute("success", "success.published"))
                .andDo(print());

        List<Account> accountList = accounts.findAll().stream()
                .filter(a -> a.getTopics().contains(spring) || a.getZones().contains(city1))
                .collect(Collectors.toList());
        assertTrue(study.isPublished());
        assertTrue(accountList.contains(user2));
        assertTrue(accountList.contains(user3));

        then(studyEventListener).should().handleStudyCreatedEvent(any(StudyCreatedEvent.class));
    }

    @DisplayName("공개된 스터디 공개 - 실패")
    @Test
    @WithAccount({"user1", "user2", "user3", "user4", "user5"})
    void publishPublishedStudy_failure() throws Exception {
        Study study = studyFactory.createStudy("user1");
        study.publish();

        String settingsStudyPublishedUrl = replacePath(study.getPath(), SETTINGS_STUDY_URL);

        this.mockMvc.perform(post(settingsStudyPublishedUrl +"/publish").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andDo(print());
    }

    @DisplayName("종료된 스터디 공개 - 실패")
    @Test
    @WithAccount({"user1", "user2", "user3", "user4", "user5"})
    void publishClosedStudy_failure() throws Exception {
        Study study = studyFactory.createStudy("user1");
        study.publish();
        study.close();

        String settingsStudyPublishedUrl = replacePath(study.getPath(), SETTINGS_STUDY_URL);

        this.mockMvc.perform(post(settingsStudyPublishedUrl +"/publish").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andDo(print());
    }

    @DisplayName("공개된 스터디 종료 - 성공")
    @Test
    @WithAccount("user1")
    void closePublishedStudy_success() throws Exception {
        Study study = studyFactory.createStudy("user1");
        study.publish();
        String settingsStudyPublishedUrl = replacePath(study.getPath(), SETTINGS_STUDY_URL);

        this.mockMvc.perform(post(settingsStudyPublishedUrl +"/close").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(settingsStudyPublishedUrl))
                .andExpect(flash().attribute("success", "success.closed"))
                .andDo(print());

        assertTrue(study.isClosed());

        then(studyEventListener).should().handleStudyUpdatedEvent(any(StudyUpdatedEvent.class));
    }

    @DisplayName("공개되지 않은 스터디 종료 - 실패")
    @Test
    @WithAccount("user1")
    void closeNotPublishStudy_failure() throws Exception {
        Study study = studyFactory.createStudy("user1");
        String settingsStudyPublishedUrl = replacePath(study.getPath(), SETTINGS_STUDY_URL);

        this.mockMvc.perform(post(settingsStudyPublishedUrl +"/close").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andDo(print());
    }

    @DisplayName("종료된 스터디 종료 - 실패")
    @Test
    @WithAccount("user1")
    void closeClosedStudy_failure() throws Exception {
        Study study = studyFactory.createStudy("user1");
        study.publish();
        study.close();
        String settingsStudyPublishedUrl = replacePath(study.getPath(), SETTINGS_STUDY_URL);

        this.mockMvc.perform(post(settingsStudyPublishedUrl +"/close").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andDo(print());
    }

    @DisplayName("스터디 모집 시작 - 성공")
    @Test
    @WithAccount("user1")
    void startRecruiting_success() throws Exception {
        Study study = studyFactory.createStudy("user1");
        study.publish();
        String settingsStudyUrl = replacePath(study.getPath(), SETTINGS_STUDY_URL);

        this.mockMvc.perform(post(settingsStudyUrl +"/recruiting/start").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(settingsStudyUrl))
                .andExpect(flash().attribute("success", "success.recruiting.start"))
                .andDo(print());

        assertTrue(study.isRecruiting());

        then(studyEventListener).should().handleStudyUpdatedEvent(any(StudyUpdatedEvent.class));
    }

    @DisplayName("미공개 스터디 모집 시작 - 실패")
    @Test
    @WithAccount("user1")
    void startNotPublicStudyRecruiting_failure() throws Exception {
        Study study = studyFactory.createStudy("user1");
        String settingsStudyUrl = replacePath(study.getPath(), SETTINGS_STUDY_URL);

        this.mockMvc.perform(post(settingsStudyUrl +"/recruiting/start").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andDo(print());
    }

    @DisplayName("종료된 스터디 모집 시작 - 실패")
    @Test
    @WithAccount("user1")
    void startClosedStudyRecruiting_failure() throws Exception {
        Study study = studyFactory.createStudy("user1");
        study.publish();
        study.close();
        String settingsStudyUrl = replacePath(study.getPath(), SETTINGS_STUDY_URL);

        this.mockMvc.perform(post(settingsStudyUrl +"/recruiting/start").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andDo(print());
    }

    @DisplayName("1시간안에 스터디 모집 시작 두번 - 실패")
    @Test
    @WithAccount("user1")
    void startStudyRecruitmentTwiceIn1Hour_failure() throws Exception {
        Study study = studyFactory.createStudy("user1");
        study.publish();
        study.startRecruiting();
        String settingsStudyUrl = replacePath(study.getPath(), SETTINGS_STUDY_URL);

        this.mockMvc.perform(post(settingsStudyUrl +"/recruiting/start").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andDo(print());
    }
    
    @DisplayName("스터디 모집 중단 - 성공")
    @Test
    @WithAccount("user1")
    void stopRecruiting_success() throws Exception {
        Study study = studyFactory.createStudy("user1");
        study.publish();
        study.setRecruiting(true);
        String settingsStudyUrl = replacePath(study.getPath(), SETTINGS_STUDY_URL);

        this.mockMvc.perform(post(settingsStudyUrl +"/recruiting/stop").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(settingsStudyUrl))
                .andExpect(flash().attribute("success", "success.recruiting.stop"))
                .andDo(print());

        assertFalse(study.isRecruiting());

        then(studyEventListener).should().handleStudyUpdatedEvent(any(StudyUpdatedEvent.class));
    }

    @DisplayName("스터디 경로 수정 - 성공")
    @Test
    @WithAccount("user1")
    void updateStudyPath_success() throws Exception {
        Study study = studyFactory.createStudy("user1");
        String settingsStudyUrl = replacePath(study.getPath(), SETTINGS_STUDY_URL);
        String path = "new-new-path";

        this.mockMvc.perform(post(settingsStudyUrl +"/path").with(csrf())
                        .param("path", path)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(replacePath(path, SETTINGS_STUDY_URL)))
                .andExpect(flash().attribute("success", "success.path"))
                .andDo(print());

        assertEquals(path, study.getPath());
    }

    @DisplayName("스터디 이름 수정 - 성공")
    @Test
    @WithAccount("user1")
    void updateStudyTitle_success() throws Exception {
        Study study = studyFactory.createStudy("user1");
        String settingsStudyUrl = replacePath(study.getPath(), SETTINGS_STUDY_URL);
        String title = "new-new-title";

        this.mockMvc.perform(post(settingsStudyUrl +"/title").with(csrf())
                        .param("title", title)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(settingsStudyUrl))
                .andExpect(flash().attribute("success", "success.title"))
                .andDo(print());

        assertEquals(title, study.getTitle());
    }

    @DisplayName("스터디 삭제")
    @Test
    @WithAccount("user1")
    void deleteStudy() throws Exception {
        Study study = studyFactory.createStudy("user1");
        String settingsStudyUrl = replacePath(study.getPath(), SETTINGS_STUDY_URL);

        this.mockMvc.perform(post(settingsStudyUrl +"/delete").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andDo(print());

        assertTrue(studys.findById(study.getId()).isEmpty());
    }

    private void requestTopic(String title, String url, ResultMatcher status) throws Exception {
        this.mockMvc.perform(post(url)
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

    private void requestZone(String zoneName, String url, ResultMatcher status) throws Exception {
        this.mockMvc.perform(post(url)
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
}