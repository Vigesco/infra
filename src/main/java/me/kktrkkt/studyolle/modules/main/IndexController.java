package me.kktrkkt.studyolle.modules.main;

import lombok.RequiredArgsConstructor;
import me.kktrkkt.studyolle.modules.account.AccountRepository;
import me.kktrkkt.studyolle.modules.account.CurrentUser;
import me.kktrkkt.studyolle.modules.account.entity.Account;
import me.kktrkkt.studyolle.modules.event.Enrollment;
import me.kktrkkt.studyolle.modules.event.EnrollmentRepository;
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

    private final AccountRepository accounts;

    private final EnrollmentRepository enrollments;

    @GetMapping("/")
    public String indexPage(@CurrentUser Account account, Model model) {
        if(account == null){
            List<Study> studyList = studys.findTop9ByPublishedTrueOrderByPublishedAtDesc();
            model.addAttribute("studyList", studyList);
            return "index";
        }
        else {
            Account accountWithTopicAndZone = accounts.findWithTopicAndZoneAndAuthorityById(account.getId()).orElseThrow();
            List<Enrollment> enrollmentList = enrollments.findAllByAccountAndAcceptedTrueAndAttendedFalseOrderByEnrolledAtDesc(account);
            List<Study> studyList = studys.findByAccountTopicAndZone(accountWithTopicAndZone);
            List<Study> studyManagerOf = studys.findTop5ByClosedFalseAndManagersContainsOrderByCreatedAtDesc(account);
            List<Study> studyMemberOf = studys.findTop5ByPublishedTrueAndMembersContainsOrderByPublishedAtDesc(account);
            model.addAttribute("account", accountWithTopicAndZone);
            model.addAttribute("enrollmentList", enrollmentList);
            model.addAttribute("studyList", studyList);
            model.addAttribute("studyManagerOf", studyManagerOf);
            model.addAttribute("studyMemberOf", studyMemberOf);
            return "index-after-login";
        }
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
