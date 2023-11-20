package me.kktrkkt.studyolle.account;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
public class SettingsController {

    private static final String PROFILE_UPDATE_FORM = "settings/profileUpdateForm";

    private final AccountService accountService;

    @GetMapping("/settings/profile")
    public String profileUpdateForm(@CurrentUser Account account, Model model) {
        model.addAttribute(new ProfileUpdateForm(account));
        return PROFILE_UPDATE_FORM;
    }

    @PostMapping("/settings/profile")
    public String updateProfile(@CurrentUser Account account, @Valid ProfileUpdateForm profileUpdateForm, Errors errors, Model model) {
        if(errors.hasErrors()){
            return PROFILE_UPDATE_FORM;
        }
        accountService.updateProfile(account, profileUpdateForm);
        model.addAttribute("success", "success");
        return PROFILE_UPDATE_FORM;
    }
}
