package kr.co.r2soft.modules.account;

import kr.co.r2soft.infra.MockMvcTest;
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
public class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @DisplayName("프로필 조회 테스트 - 주인")
    @Test
    @WithAccount("user1")
    public void profileView_with_owner() throws Exception {
        this.mockMvc.perform(get(ProfileController.PROFILE_URL+"/user1"))
                .andExpect(status().isOk())
                .andExpect(view().name(ProfileController.PROFILE_VIEW))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attribute("isOwner", true))
                .andDo(print());
    }

    @DisplayName("프로필 조회 테스트 - 인증유저")
    @Test
    @WithAccount({"user2", "user1"})
    public void profileView_with_authenticationUser() throws Exception {
        this.mockMvc.perform(get(ProfileController.PROFILE_URL+"/user1"))
                .andExpect(status().isOk())
                .andExpect(view().name(ProfileController.PROFILE_VIEW))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attribute("isOwner", false))
                .andDo(print());
    }
}
