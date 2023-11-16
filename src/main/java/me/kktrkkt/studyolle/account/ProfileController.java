package me.kktrkkt.studyolle.account;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class ProfileController {

    private static final String PROFILE_VIEW = "profile/view";

    private final AccountRepository accounts;

    @GetMapping("/profile/{nickname}")
    public String viewProfile(@PathVariable String nickname, Model model, @CurrentUser Account currentUser) {
        Account account = accounts.findByNickname(nickname)
                .orElseThrow(() -> new NicknameNotFoundException(nickname));

        model.addAttribute(account);
        model.addAttribute("isOwner", currentUser.getNickname().equals(nickname));

        return PROFILE_VIEW;
    }
}
