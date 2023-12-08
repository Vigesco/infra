package me.kktrkkt.studyolle.study;

import me.kktrkkt.studyolle.account.AccountRepository;
import me.kktrkkt.studyolle.account.WithAccount;
import me.kktrkkt.studyolle.account.entity.Account;
import me.kktrkkt.studyolle.infra.MockMvcTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

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

    @DisplayName("스터디 설정 소개 조회")
    @Test
    @WithAccount("user1")
    void studySettingsInfoForm() throws Exception {
        Study study = createStudy(accounts.findByNickname("user1").get());

        this.mockMvc.perform(get(SETTINGS_INFO_URL.replace("{path}", study.getPath())))
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
        String studySettingsInfoUrl = SETTINGS_INFO_URL.replace("{path}", study.getPath());

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
        String studySettingsInfoUrl = SETTINGS_INFO_URL.replace("{path}", study.getPath());

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

        this.mockMvc.perform(get(SETTINGS_BANNER_URL.replace("{path}", study.getPath())))
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
        String studySettingsBannerUrl = SETTINGS_BANNER_URL.replace("{path}", study.getPath());

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
        String studySettingsBannerUrl = SETTINGS_BANNER_URL.replace("{path}", study.getPath());

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
}