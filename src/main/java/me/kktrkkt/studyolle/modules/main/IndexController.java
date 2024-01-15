package me.kktrkkt.studyolle.modules.main;

import lombok.RequiredArgsConstructor;
import me.kktrkkt.studyolle.modules.study.Study;
import me.kktrkkt.studyolle.modules.study.StudyRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class IndexController {

    private final StudyRepository studyRepository;

    @GetMapping("/")
    public String indexPage() {
        return "index";
    }

    @GetMapping("/search/study")
    public String searchStudy(@RequestParam(defaultValue = "") String keyword, Model model) {
        List<Study> all = studyRepository.findByKeyword(keyword);
        model.addAttribute("studyList", all);
        model.addAttribute("keyword", keyword);
        return "search";
    }

}
