package me.kktrkkt.studyolle.account;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class SettingsController {

    private static final String PROFILE_UPDATE_FORM = "settings/profileUpdateForm";

    @ModelAttribute(name = "account")
    public Account currentUser(@CurrentUser Account account){
        return account;
    }

    @GetMapping("/settings/profile")
    public String profileUpdateForm() {
        return PROFILE_UPDATE_FORM;
    }
}
