package me.kktrkkt.studyolle.study;

import me.kktrkkt.studyolle.account.WithAccount;
import me.kktrkkt.studyolle.infra.MockMvcTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
        this.mockMvc.perform(get(StudyController.NEW_STUDY_URL))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("studyForm"))
                .andExpect(view().name(StudyController.NEW_STUDY_VIEW))
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
        this.mockMvc.perform(post(StudyController.NEW_STUDY_URL)
                        .with(csrf())
                        .param("path", path)
                        .param("title", title)
                        .param("bio", bio)
                        .param("explanation", explanation)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(StudyController.STUDY_URL+"/"+ URLEncoder.encode(path, StandardCharsets.UTF_8)))
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
        this.mockMvc.perform(post(StudyController.NEW_STUDY_URL)
                        .with(csrf())
                        .param("path", path)
                        .param("title", title)
                        .param("bio", bio)
                        .param("explanation", explanation)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(view().name(StudyController.NEW_STUDY_VIEW))
                .andExpect(model().attributeExists("studyForm"))
                .andExpect(model().hasErrors())
                .andDo(print());
        Optional<Study> byUrl = studys.findByPath(path);
        assertFalse(byUrl.isPresent());
    }
}