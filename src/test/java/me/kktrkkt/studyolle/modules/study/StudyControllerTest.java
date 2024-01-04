package me.kktrkkt.studyolle.modules.study;

import me.kktrkkt.studyolle.WithAccount;
import me.kktrkkt.studyolle.modules.account.entity.Account;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static me.kktrkkt.studyolle.modules.study.StudyController.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
public class StudyControllerTest extends StudyBaseTest {

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
                .andExpect(redirectedUrl(replacePath(path, STUDY_BASE_URL)))
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
        Study study = createStudy("user1");

        this.mockMvc.perform(get(replacePath(study.getPath(), STUDY_BASE_URL)))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("study"))
                .andExpect(view().name(STUDY_VIEW))
                .andDo(print());
    }

    @DisplayName("스터디 구성원 조회")
    @Test
    @WithAccount("user1")
    void studyMembers() throws Exception {
        Study study = createStudy("user1");

        this.mockMvc.perform(get(replacePath(study.getPath(), STUDY_MEMBERS_URL)))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("study"))
                .andExpect(view().name(STUDY_MEMBERS_VIEW))
                .andDo(print());
    }

    @DisplayName("스터디 참여")
    @Test
    @WithAccount({"user1", "user2"})
    void joinStudy() throws Exception {
        Study study = createStudy("user2");
        study.publish();
        study.startRecruiting();

        String studyBaseUrl = replacePath(study.getPath(), STUDY_BASE_URL);

        this.mockMvc.perform(post(studyBaseUrl + "/join").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(studyBaseUrl))
                .andDo(print());

        assertTrue(study.getMembers().contains(accounts.findByNickname("user1").get()));
    }

    @DisplayName("스터디 탈퇴")
    @Test
    @WithAccount({"user1", "user2"})
    void leaveStudy() throws Exception {
        Study study = createStudy("user2");
        study.publish();
        study.startRecruiting();
        Account user1 = accounts.findByNickname("user1").get();
        study.addMember(user1);

        String studyBaseUrl = replacePath(study.getPath(), STUDY_BASE_URL);

        this.mockMvc.perform(post(studyBaseUrl+"/leave").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(studyBaseUrl))
                .andDo(print());

        assertFalse(study.getMembers().contains(user1));
    }
}