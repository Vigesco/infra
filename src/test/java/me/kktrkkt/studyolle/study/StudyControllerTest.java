package me.kktrkkt.studyolle.study;

import me.kktrkkt.studyolle.account.WithAccount;
import me.kktrkkt.studyolle.infra.MockMvcTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Random;

import static me.kktrkkt.studyolle.study.StudyController.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@MockMvcTest
@Transactional
class StudyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StudyRepository studys;

    @DisplayName("스터디 생성 폼 조회")
    @Test
    @WithAccount("user1")
    void newStudySubmitForm() throws Exception {
        this.mockMvc.perform(get(NEW_STUDY_URL))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("studyForm"))
                .andExpect(view().name(NEW_STUDY_VIEW))
                .andDo(print());
    }

    @DisplayName("스터디 생성 - 성공")
    @Test
    @WithAccount("user1")
    void createStudy_success() throws Exception {
        String path = "new-study";
        String title = "new-study";
        String bio = "bio";
        String explanation = "explanation";
        this.mockMvc.perform(post(NEW_STUDY_URL)
                        .with(csrf())
                        .param("path", path)
                        .param("title", title)
                        .param("bio", bio)
                        .param("explanation", explanation)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(STUDY_URL+"/"+path))
                .andExpect(flash().attributeExists("study"))
                .andDo(print());
        Optional<Study> byUrl = studys.findByPath(path);
        assertTrue(byUrl.isPresent());
    }

    @DisplayName("스터디 생성 - 실패")
    @Test
    @WithAccount("user1")
    void createStudy_failure() throws Exception {
        String path = "new/study";
        String title = "new-study";
        String bio = "bio";
        String explanation = "explanation";
        this.mockMvc.perform(post(NEW_STUDY_URL)
                        .with(csrf())
                        .param("path", path)
                        .param("title", title)
                        .param("bio", bio)
                        .param("explanation", explanation)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(view().name(NEW_STUDY_VIEW))
                .andExpect(model().attributeExists("studyForm"))
                .andExpect(model().hasErrors())
                .andDo(print());
        Optional<Study> byUrl = studys.findByPath(path);
        assertFalse(byUrl.isPresent());
    }

    @DisplayName("스터디 소개 조회")
    @Test
    @WithAccount("user1")
    void studyView() throws Exception {
        Study study = createStudy();

        this.mockMvc.perform(get(STUDY_URL + "/" + study.getPath()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("study"))
                .andExpect(view().name(STUDY_VIEW))
                .andDo(print());
    }

    @DisplayName("스터디 구성원 조회")
    @Test
    @WithAccount("user1")
    void studyMembers() throws Exception {
        Study study = createStudy();

        this.mockMvc.perform(get(STUDY_URL + "/" + study.getPath() + MEMBERS_URL))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("study"))
                .andExpect(view().name(STUDY_MEMBERS_VIEW))
                .andDo(print());
    }

    @DisplayName("스터디 설정 소개 조회")
    @Test
    @WithAccount("user1")
    void studySettingsInfoForm() throws Exception {
        Study study = createStudy();

        this.mockMvc.perform(get(STUDY_URL + "/" + study.getPath() + SETTINGS_INFO_URL))
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
        Study study = createStudy();
        String studySettingsInfoUrl = STUDY_URL + "/" + study.getPath() + SETTINGS_INFO_URL;

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
        Study study = createStudy();
        String studySettingsInfoUrl = STUDY_URL + "/" + study.getPath() + SETTINGS_INFO_URL;

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

        Study byId = studys.findById(study.getId()).get();
        assertNotEquals(bio, byId.getBio());
        assertNotEquals(explanation, byId.getExplanation());
    }

    private Study createStudy() {
        String path = "new-study";
        String title = "new-study";
        String bio = "bio";
        String explanation = "explanation";

        Study newStudy = new Study();
        newStudy.setPath(path);
        newStudy.setTitle(title);
        newStudy.setBio(bio);
        newStudy.setExplanation(explanation);

        return studys.save(newStudy);
    }
}