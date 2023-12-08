package me.kktrkkt.studyolle.study;

import lombok.RequiredArgsConstructor;
import me.kktrkkt.studyolle.account.CurrentUser;
import me.kktrkkt.studyolle.account.entity.Account;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
public class StudySettingsController {

    static final String SETTINGS_BASE_URL = "/study/{path}/settings";
    static final String SETTINGS_INFO_URL = SETTINGS_BASE_URL + "/info";
    static final String SETTINGS_INFO_VIEW = "study/settings/info";
    static final String SETTINGS_BANNER_URL = SETTINGS_BASE_URL + "/banner";
    static final String SETTINGS_BANNER_VIEW = "study/settings/banner";

    private final StudyService studyService;

    private final ModelMapper modelMapper;

    @ModelAttribute
    public Study getStudyToUpdate(@PathVariable String path, @CurrentUser Account account){
        return studyService.getStudyToUpdate(account, path);
    }

    @GetMapping(SETTINGS_INFO_URL)
    public String studySettingsInfo(Study study, Model model) {
        model.addAttribute(modelMapper.map(study, StudyInfoForm.class));
        return SETTINGS_INFO_VIEW;
    }

    @PostMapping(SETTINGS_INFO_URL)
    public String updateStudyInfo(Study study, @Valid StudyInfoForm infoForm, Errors errors,
                                  RedirectAttributes ra) {
        if(errors.hasErrors()){
            return SETTINGS_INFO_VIEW;
        }
        else {
            studyService.updateInfo(study, infoForm);
            ra.addFlashAttribute("success", "success");
            return "redirect:" + SETTINGS_INFO_URL;
        }
    }

    @GetMapping(SETTINGS_BANNER_URL)
    public String studySettingsBanner() {
        return SETTINGS_BANNER_VIEW;
    }

    @PostMapping(SETTINGS_BANNER_URL)
    public String updateStudyBanner(Study study, String banner,
                                    Errors errors, RedirectAttributes ra) {
        if(errors.hasErrors()){
            return SETTINGS_BANNER_VIEW;
        }
        else {
            studyService.updateBanner(study, banner);
            ra.addFlashAttribute("success", "success.banner");
            return "redirect:" + SETTINGS_BANNER_URL;
        }
    }

    @PostMapping(SETTINGS_BANNER_URL + "/{use}")
    public String updateStudyBannerUse(Study study, @PathVariable boolean use,
                                    Errors errors, RedirectAttributes ra) {
        if(errors.hasErrors()){
            return SETTINGS_BANNER_VIEW;
        }
        else {
            studyService.updateBannerUse(study, use);
            ra.addFlashAttribute("success", "success.useBanner");
            return "redirect:" + SETTINGS_BANNER_URL;
        }
    }
}
