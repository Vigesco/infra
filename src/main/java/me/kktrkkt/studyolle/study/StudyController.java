package me.kktrkkt.studyolle.study;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequiredArgsConstructor
public class StudyController {

    static final String NEW_STUDY_URL = "/new-study";
    static final String NEW_STUDY_VIEW = "study/newStudySubmitForm";

    private final StudyService studyService;

    @GetMapping(NEW_STUDY_URL)
    public String newStudyForm(Model model) {
        model.addAttribute(new StudyForm());
        return NEW_STUDY_VIEW;
    }

    @PostMapping(NEW_STUDY_URL)
    public String createStudy(@Valid StudyForm studySubmitForm, Errors errors, RedirectAttributes ra) {
        if(errors.hasErrors()){
            return NEW_STUDY_VIEW;
        }
        else{
            Study study = studyService.create(studySubmitForm);
            ra.addFlashAttribute(study);
            return "redirect:/study/" + URLEncoder.encode(study.getPath(), StandardCharsets.UTF_8);
        }
    }
}
