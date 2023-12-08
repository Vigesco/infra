package me.kktrkkt.studyolle.study;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mysema.commons.lang.Assert;
import me.kktrkkt.studyolle.account.AccountRepository;
import me.kktrkkt.studyolle.account.AccountService;
import me.kktrkkt.studyolle.account.SettingsController;
import me.kktrkkt.studyolle.account.WithAccount;
import me.kktrkkt.studyolle.account.entity.Account;
import me.kktrkkt.studyolle.infra.MockMvcTest;
import me.kktrkkt.studyolle.topic.Topic;
import me.kktrkkt.studyolle.topic.TopicForm;
import me.kktrkkt.studyolle.topic.TopicRepository;
import me.kktrkkt.studyolle.zone.Zone;
import me.kktrkkt.studyolle.zone.ZoneForm;
import me.kktrkkt.studyolle.zone.ZoneRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import static me.kktrkkt.studyolle.study.StudySettingsController.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@MockMvcTest
@Transactional
class StudySettingsControllerTest {

    @Autowired
    private MockMvc mockMvc;

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

    @DisplayName("스터디 설정 소개 조회")
    @Test
    @WithAccount("user1")
    void studySettingsInfoForm() throws Exception {
        Study study = createStudy(accounts.findByNickname("user1").get());

        this.mockMvc.perform(get(replacePath(study, SETTINGS_INFO_URL)))
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
        Study study = createStudy(accounts.findByNickname("user1").get());
        String studySettingsInfoUrl = replacePath(study, SETTINGS_INFO_URL);

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
    }

    @DisplayName("스터디 소개 설정 - 실패")
    @Test
    @WithAccount("user1")
    void updateStudyInfo_failure() throws Exception {
        Study study = createStudy(accounts.findByNickname("user1").get());
        String studySettingsInfoUrl = replacePath(study, SETTINGS_INFO_URL);

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
        Study study = createStudy(accounts.findByNickname("user1").get());

        this.mockMvc.perform(get(replacePath(study, SETTINGS_BANNER_URL)))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("study"))
                .andExpect(view().name(SETTINGS_BANNER_VIEW))
                .andDo(print());
    }

    @DisplayName("스터디 배너 사용 설정")
    @Test
    @WithAccount("user1")
    void updateStudyBannerUse_success() throws Exception {
        Study study = createStudy(accounts.findByNickname("user1").get());
        String studySettingsBannerUrl = replacePath(study, SETTINGS_BANNER_URL);

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
        Study study = createStudy(accounts.findByNickname("user1").get());
        String studySettingsBannerUrl = replacePath(study, SETTINGS_BANNER_URL);

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
        Study study = createStudy(accounts.findByNickname("user1").get());

        this.mockMvc.perform(get(replacePath(study, SETTINGS_TOPIC_URL)))
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
        Study study = createStudy(accounts.findByNickname("user1").get());
        String spring = "스프링";
        requestTopic(spring, replacePath(study, SETTINGS_TOPIC_URL) + "/add", status().isOk());

        Optional<Topic> springTopic = topics.findByTitle(spring);
        Assertions.assertTrue(springTopic.isPresent());
        Assertions.assertTrue(study.getTopics().contains(springTopic.get()));
    }

    @DisplayName("스터디 관심주제 중복 추가 - 성공")
    @Test
    @WithAccount("user1")
    void addDuplicationStudyTopic_success() throws Exception {
        Study study = createStudy(accounts.findByNickname("user1").get());
        Topic topic = new Topic();
        String title = "스프링";
        topic.setTitle(title);
        topics.save(topic);

        requestTopic(title, replacePath(study, SETTINGS_TOPIC_URL) + "/add", status().isOk());

        List<Topic> topicAll  = topics.findAll();
        Assertions.assertEquals(1, topicAll.stream().filter(x -> x.getTitle().equals(title)).count());
    }

    @DisplayName("스터디 관심주제 추가 - 실패")
    @Test
    @WithAccount("user1")
    void addStudyTopic_failure() throws Exception {
        Study study = createStudy(accounts.findByNickname("user1").get());

        requestTopic("스", replacePath(study, SETTINGS_TOPIC_URL) + "/add", status().isBadRequest());
        requestTopic("1", replacePath(study, SETTINGS_TOPIC_URL) + "/add", status().isBadRequest());
        requestTopic("스 프 링", replacePath(study, SETTINGS_TOPIC_URL) + "/add", status().isBadRequest());
        requestTopic("가나다라마가나다라마가나다라마가나다라마가", replacePath(study, SETTINGS_TOPIC_URL) + "/add", status().isBadRequest());
        requestTopic("ㄱ", replacePath(study, SETTINGS_TOPIC_URL) + "/add", status().isBadRequest());

        Assertions.assertTrue(study.getTopics().isEmpty());
    }


    @DisplayName("스터디 관심주제 삭제 - 성공")
    @Test
    @WithAccount("user1")
    void removeStudyTopic_success() throws Exception {
        Study study = createStudy(accounts.findByNickname("user1").get());

        String spring = "스프링";
        requestTopic(spring, replacePath(study, SETTINGS_TOPIC_URL) + "/add", status().isOk());
        requestTopic(spring, replacePath(study, SETTINGS_TOPIC_URL) + "/remove", status().isOk());

        Optional<Topic> springTopic = topics.findByTitle(spring);
        Assertions.assertTrue(springTopic.isPresent());
        Assertions.assertTrue(study.getTopics().isEmpty());
    }

    @DisplayName("스터디 관심주제 삭제 - 실패")
    @Test
    @WithAccount("user1")
    void removeStudyTopic_failure() throws Exception {
        Study study = createStudy(accounts.findByNickname("user1").get());

        String spring = "스프링";
        requestTopic(spring, replacePath(study, SETTINGS_TOPIC_URL) + "/remove", status().isBadRequest());

        Optional<Topic> springTopic = topics.findByTitle(spring);
        Assertions.assertTrue(springTopic.isEmpty());
        Assertions.assertTrue(study.getTopics().isEmpty());
    }

    @DisplayName("스터디 설정 활동지역 조회")
    @Test
    @WithAccount("user1")
    void studySettingsZoneForm() throws Exception {
        Study study = createStudy(accounts.findByNickname("user1").get());

        this.mockMvc.perform(get(replacePath(study, SETTINGS_ZONE_URL)))
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
        Study study = createStudy(accounts.findByNickname("user1").get());
        Zone testZone = Zone.builder().city("test").localNameOfCity("테스트").province("testp").build();
        zones.save(testZone);

        requestZone(testZone.toString(), replacePath(study, SETTINGS_ZONE_URL) +"/add", status().isOk());

        Assertions.assertTrue(study.getZones().contains(testZone));
    }

    @DisplayName("스터디 주요지역 추가 - 실패")
    @Test
    @WithAccount("user1")
    void addStudyZone_failure() throws Exception {
        Study study = createStudy(accounts.findByNickname("user1").get());

        requestZone("wrong(Zone)/Name", replacePath(study, SETTINGS_ZONE_URL) +"/add", status().isBadRequest());

        Assertions.assertTrue(study.getZones().isEmpty());
    }

    @DisplayName("스터디 주요지역 삭제 - 성공")
    @Test
    @WithAccount("user1")
    void
    _success() throws Exception {
        Study study = createStudy(accounts.findByNickname("user1").get());

        Zone testZone = Zone.builder().city("test").localNameOfCity("테스트").province("testp").build();
        zones.save(testZone);

        requestZone(testZone.toString(), replacePath(study, SETTINGS_ZONE_URL) +"/add", status().isOk());
        requestZone(testZone.toString(), replacePath(study, SETTINGS_ZONE_URL) +"/remove", status().isOk());

        Assertions.assertTrue(zones.findById(testZone.getId()).isPresent());
        Assertions.assertTrue(study.getZones().isEmpty());
    }

    @DisplayName("스터디 주요지역 삭제 - 실패")
    @Test
    @WithAccount("user1")
    void removeStudyZone_failure() throws Exception {
        Study study = createStudy(accounts.findByNickname("user1").get());

        requestZone("wrong(Zone)/Name", replacePath(study, SETTINGS_ZONE_URL) +"/remove", status().isBadRequest());

        Assertions.assertTrue(study.getZones().isEmpty());
    }

    private Study createStudy(Account account) {
        String path = "new-study";
        String title = "new-study";
        String bio = "bio";
        String explanation = "explanation";

        Study newStudy = new Study();
        newStudy.setPath(path);
        newStudy.setTitle(title);
        newStudy.setBio(bio);
        newStudy.setExplanation(explanation);
        newStudy.getManagers().add(account);

        return studys.save(newStudy);
    }

    private String replacePath(Study study, String settingsZoneUrl) {
        return settingsZoneUrl.replace("{path}", study.getPath());
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