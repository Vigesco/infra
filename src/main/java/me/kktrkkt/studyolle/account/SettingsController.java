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

    static final String PROFILE_UPDATE_VIEW = "settings/profileUpdateForm";
    static final String SETTINGS_PROFILE_URL = "/settings/profile";
    static final String NOTIFICATION_UPDATE_VIEW = "settings/notificationUpdateForm";
    static final String SETTINGS_NOTIFICATION_URL = "/settings/notification";

    private final AccountService accountService;

    private final ModelMapper modelMapper;

    @GetMapping(SETTINGS_PROFILE_URL)
    public String profileUpdateForm(@CurrentUser Account account, Model model) {
        ProfileUpdateForm profileUpdateForm = modelMapper.map(account, ProfileUpdateForm.class);
        model.addAttribute(profileUpdateForm);
        return PROFILE_UPDATE_VIEW;
    }

    @PostMapping(SETTINGS_PROFILE_URL)
    public String updateProfile(@CurrentUser Account account, @Valid ProfileUpdateForm profileUpdateForm, Errors errors, RedirectAttributes ra) {
        if(errors.hasErrors()){
            return PROFILE_UPDATE_VIEW;
        }
        accountService.save(account, profileUpdateForm);
        ra.addFlashAttribute("success", "success");
        return "redirect:" + SETTINGS_PROFILE_URL;
    }

    @GetMapping(SETTINGS_NOTIFICATION_URL)
    public String notificationUpdateForm(@CurrentUser Account account, Model model) {
        NotificationUpdateForm notificationUpdateForm = modelMapper.map(account, NotificationUpdateForm.class);
        model.addAttribute(notificationUpdateForm);
        return NOTIFICATION_UPDATE_VIEW;
    }

    @PostMapping(SETTINGS_NOTIFICATION_URL)
    public String saveNotification(@CurrentUser Account account, NotificationUpdateForm notificationUpdateForm, Errors errors, RedirectAttributes ra) {
        if(errors.hasErrors()){
            return NOTIFICATION_UPDATE_VIEW;
        }
        accountService.save(account, notificationUpdateForm);
        ra.addFlashAttribute("success", "success");
        return "redirect:" + SETTINGS_NOTIFICATION_URL;
    }
}
