package me.kktrkkt.studyolle.study;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StudyController {

    private static final String NEW_STUDY_URL = "/new-study";
    private static final String NEW_STUDY_VIEW = "study/studySubmitForm";

    @GetMapping(NEW_STUDY_URL)
    public String studySubmitForm(Model model) {
        model.addAttribute(new StudySubmitForm());
        return NEW_STUDY_VIEW;
    }
}
