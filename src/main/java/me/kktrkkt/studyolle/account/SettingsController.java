package me.kktrkkt.studyolle.account;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class SettingsController {

    private static final String PROFILE_UPDATE_FORM = "settings/profileUpdateForm";

    @GetMapping("/settings/profile")
    public String profileUpdateForm() {
        return PROFILE_UPDATE_FORM;
    }
}
