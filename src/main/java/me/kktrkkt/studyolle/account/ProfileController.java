package me.kktrkkt.studyolle.account;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class ProfileController {

    static final String PROFILE_VIEW = "profile/view";
    static final String PROFILE_URL = "/profile";

    private final AccountRepository accounts;

    @GetMapping(PROFILE_URL + "/{nickname}")
    public String viewProfile(@PathVariable String nickname, Model model, @CurrentUser Account currentUser) {
        Account account = accounts.findByNickname(nickname)
                .orElseThrow(() -> new NicknameNotFoundException(nickname));

        model.addAttribute(account);
        model.addAttribute("isOwner", currentUser.getNickname().equals(nickname));

        return PROFILE_VIEW;
    }
}
