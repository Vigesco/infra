package me.kktrkkt.studyolle.event;

import lombok.RequiredArgsConstructor;
import me.kktrkkt.studyolle.account.CurrentUser;
import me.kktrkkt.studyolle.account.entity.Account;
import me.kktrkkt.studyolle.study.Study;
import me.kktrkkt.studyolle.study.StudyService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/study/{path}")
@RequiredArgsConstructor
public class EventController {

    static final String NEW_EVENT_FORM = "event/newEventForm";

    private final StudyService studyService;

    @ModelAttribute
    public Study getStudyToUpdate(@PathVariable String path, @CurrentUser Account account){
        return studyService.getStudyToUpdateStatus(account, path);
    }

    @GetMapping("/new-event")
    public String newEventForm(Model model) {
        model.addAttribute("eventTypes", EventType.values());
        model.addAttribute(new EventForm());
        return NEW_EVENT_FORM;
    }
}
