package me.kktrkkt.studyolle.study;

import lombok.RequiredArgsConstructor;
import me.kktrkkt.studyolle.account.CurrentUser;
import me.kktrkkt.studyolle.account.entity.Account;
import me.kktrkkt.studyolle.event.Event;
import me.kktrkkt.studyolle.event.EventRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class StudyController {

    static final String STUDY_BASE_URL = "/study/{path}";
    static final String STUDY_VIEW= "study/view";
    static final String NEW_STUDY_URL = "/new-study";
    static final String NEW_STUDY_VIEW = "study/newStudySubmitForm";
    static final String STUDY_EVENTS_URL = STUDY_BASE_URL + "/events";
    static final String STUDY_EVENTS_VIEW = "study/events";
    static final String STUDY_MEMBERS_URL = STUDY_BASE_URL + "/members";
    static final String STUDY_MEMBERS_VIEW= "study/members";

    private final StudyService studyService;
    private final EventRepository events;

    @GetMapping(NEW_STUDY_URL)
    public String newStudyForm(Model model) {
        model.addAttribute(new StudyForm());
        return NEW_STUDY_VIEW;
    }

    @PostMapping(NEW_STUDY_URL)
    public String createStudy(@CurrentUser Account account, @Valid StudyForm studySubmitForm, Errors errors, RedirectAttributes ra) {
        if(errors.hasErrors()) {
            return NEW_STUDY_VIEW;
        }
        else {
            Study study = studyService.create(account, studySubmitForm);
            ra.addFlashAttribute(study);
            return "redirect:/study/" + URLEncoder.encode(study.getPath(), StandardCharsets.UTF_8);
        }
    }

    @GetMapping(STUDY_BASE_URL)
    public String studyView(@PathVariable String path, Model model) {
        Study byPath = studyService.getStudy(path);
        model.addAttribute(byPath);
        return STUDY_VIEW;
    }

    @GetMapping(STUDY_EVENTS_URL)
    public String eventList(@PathVariable String path, @CurrentUser Account account,
                            Model model) {
        Study study = studyService.getStudyToEvent(path, account);
        List<Event> eventList = events.findByStudyOrderByCreatedDateTime(study);

        model.addAttribute(study);
        model.addAttribute("newEvents", eventList.stream()
                .filter(Event::isNew)
                .collect(Collectors.toList()));
        model.addAttribute("oldEvents", eventList.stream()
                .filter(Event::isOld)
                .collect(Collectors.toList()));
        return STUDY_EVENTS_VIEW;
    }

    @GetMapping(STUDY_MEMBERS_URL)
    public String studyMembers(@PathVariable String path, Model model) {
        Study byPath = studyService.getStudy(path);
        model.addAttribute(byPath);
        return STUDY_MEMBERS_VIEW;
    }

    @PostMapping(STUDY_BASE_URL + "/join")
    public String joinStudy(@PathVariable String path, @CurrentUser Account account) {
        Study byPath = studyService.getStudyToMember(path);
        studyService.addMember(byPath, account);
        return "redirect:" + STUDY_BASE_URL;
    }

    @PostMapping(STUDY_BASE_URL + "/leave")
    public String leaveStudy(@PathVariable String path, @CurrentUser Account account) {
        Study byPath = studyService.getStudyToMember(path);
        studyService.removeMember(byPath, account);
        return "redirect:" + STUDY_BASE_URL;
    }
}
