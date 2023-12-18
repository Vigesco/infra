package me.kktrkkt.studyolle.event;

import lombok.RequiredArgsConstructor;
import me.kktrkkt.studyolle.account.CurrentUser;
import me.kktrkkt.studyolle.account.entity.Account;
import me.kktrkkt.studyolle.study.Study;
import me.kktrkkt.studyolle.study.StudyService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
public class EventController {

    static final String BASE_URL = "/study/{path}";
    static final String NEW_EVENT_URL = BASE_URL + "/new-event";
    static final String NEW_EVENT_VIEW = "event/newEventForm";
    static final String EVENT_URL = BASE_URL + "/event/{id}";

    private final StudyService studyService;
    private final EventService eventService;

    @InitBinder("eventForm")
    public void validEventForm(WebDataBinder dataBinder){
        dataBinder.addValidators(new EventValidator());
    }

    @GetMapping(NEW_EVENT_URL)
    public String newEventForm(@PathVariable String path, @CurrentUser Account account, Model model) {
        Study study = studyService.getStudyToUpdateStatus(account, path);
        model.addAttribute(study);
        model.addAttribute("eventTypes", EventType.values());
        model.addAttribute(new EventForm());
        return NEW_EVENT_VIEW;
    }

    @PostMapping(NEW_EVENT_URL)
    public String createNewEvent(@PathVariable String path, @CurrentUser Account account,
                                 @Valid EventForm eventForm, Errors errors, Model model) {
        Study study = studyService.getStudyToUpdateStatus(account, path);
        if(errors.hasErrors()){
            model.addAttribute(study);
            model.addAttribute("eventTypes", EventType.values());
            return NEW_EVENT_VIEW;
        }
        Event newEvent = eventService.create(account, eventForm, study);
        return "redirect:" + EVENT_URL.replace("{id}", String.valueOf(newEvent.getId()));
    }
}
