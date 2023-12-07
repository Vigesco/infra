package me.kktrkkt.studyolle.study;

import lombok.RequiredArgsConstructor;
import me.kktrkkt.studyolle.account.CurrentUser;
import me.kktrkkt.studyolle.account.entity.Account;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
public class StudySettingsController {

    static final String SETTINGS_BASE_URL = "/study/{path}/settings";
    static final String SETTINGS_INFO_URL = SETTINGS_BASE_URL + "/info";
    static final String SETTINGS_INFO_VIEW = "study/settings/info";

    private final StudyService studyService;

    private final ModelMapper modelMapper;

    @GetMapping(SETTINGS_INFO_URL)
    public String studySettingsInfo(@PathVariable String path,
                                    @CurrentUser Account account,
                                    Model model) {
        Study byPath = studyService.getStudyToUpdate(account, path);
        model.addAttribute(byPath);
        model.addAttribute(modelMapper.map(byPath, StudyInfoForm.class));
        return SETTINGS_INFO_VIEW;
    }

    @PostMapping(SETTINGS_INFO_URL)
    public String updateStudyInfo(@PathVariable String path,
                                  @CurrentUser Account account,
                                  @Valid StudyInfoForm infoForm,
                                  Errors errors, Model model,
                                  RedirectAttributes ra) {
        Study byPath = studyService.getStudyToUpdate(account, path);
        if(errors.hasErrors()){
            model.addAttribute(byPath);
            return SETTINGS_INFO_VIEW;
        }
        else {
            studyService.updateInfo(byPath, infoForm);
            ra.addFlashAttribute("success", "success");
            return "redirect:" + SETTINGS_INFO_URL;
        }
    }
}
