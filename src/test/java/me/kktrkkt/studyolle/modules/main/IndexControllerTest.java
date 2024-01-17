package me.kktrkkt.studyolle.modules.main;

import me.kktrkkt.studyolle.infra.MockMvcTest;
import me.kktrkkt.studyolle.modules.account.WithAccount;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@MockMvcTest
@Transactional
class IndexControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @DisplayName("스터디 검색 조회")
    @Test
    void searchStudy() throws Exception {
        this.mockMvc.perform(get("/search/study"))
                .andExpect(status().isOk())
                .andExpect(view().name("search"))
                .andExpect(model().attributeExists("keyword"))
                .andExpect(model().attributeExists("studyList"))
                .andExpect(model().attributeExists("currentPage"))
                .andExpect(model().attributeExists("totalItems"))
                .andExpect(model().attributeExists("totalPages"))
                .andExpect(model().attributeExists("pageSize"))
                .andExpect(model().attributeExists("sortProperty"))
                .andDo(print());
    }

    @DisplayName("index 페이지 조회 - 미로그인")
    @Test
    void index_notLoggedIn() throws Exception {
        this.mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("studyList"))
                .andDo(print());
    }

    @DisplayName("index 페이지 조회 - 로그인")
    @Test
    @WithAccount("user1")
    void index_loggedIn() throws Exception {
        this.mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index-after-login"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("enrollmentList"))
                .andExpect(model().attributeExists("studyList"))
                .andExpect(model().attributeExists("studyManagerOf"))
                .andExpect(model().attributeExists("studyMemberOf"))
                .andDo(print());
    }
}