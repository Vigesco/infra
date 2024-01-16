package me.kktrkkt.studyolle.modules.main;

import lombok.RequiredArgsConstructor;
import me.kktrkkt.studyolle.modules.study.Study;
import me.kktrkkt.studyolle.modules.study.StudyRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class IndexController {

    private final StudyRepository studys;

    @GetMapping("/")
    public String indexPage(Model model) {
        List<Study> studyList = studys.findTop9ByPublishedTrueOrderByPublishedAtDesc();
        model.addAttribute("studyList", studyList);
        return "index";
    }

    @GetMapping("/search/study")
    public String searchStudy(@RequestParam(defaultValue = "1") int pageNum,
                              @RequestParam(defaultValue = "") String keyword,
                              @RequestParam(defaultValue = "publishedAt") String sortProperty,
                              Model model) {
        Pageable pageable = PageRequest.of(pageNum - 1, 9, Sort.by(Sort.Direction.DESC, sortProperty));
        Page<Study> page = studys.findByKeyword(keyword, pageable);
        List<Study> studyList = page.getContent();

        model.addAttribute("keyword", keyword);
        model.addAttribute("studyList", studyList);
        model.addAttribute("currentPage", page.getNumber() + 1);
        model.addAttribute("totalItems", page.getTotalElements());
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("pageSize", page.getSize());
        model.addAttribute("sortProperty", sortProperty);
        return "search";
    }

}
