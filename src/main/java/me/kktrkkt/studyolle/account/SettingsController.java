package me.kktrkkt.studyolle.account;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
public class SettingsController {

    private static final String PROFILE_UPDATE_FORM = "settings/profileUpdateForm";

    private final AccountService accountService;

    private final ModelMapper modelMapper;

    @GetMapping("/settings/profile")
    public String profileUpdateForm(@CurrentUser Account account, Model model) {
        ProfileUpdateForm profileUpdateForm = modelMapper.map(account, ProfileUpdateForm.class);
        model.addAttribute(profileUpdateForm);
        return PROFILE_UPDATE_FORM;
    }

    @PostMapping("/settings/profile")
    public String updateProfile(@CurrentUser Account account, @Valid ProfileUpdateForm profileUpdateForm, Errors errors, RedirectAttributes ra) {
        if(errors.hasErrors()){
            return PROFILE_UPDATE_FORM;
        }
        accountService.updateProfile(account, profileUpdateForm);
        ra.addFlashAttribute("success", "success");
        return "redirect:/settings/profile";
    }
}
