package me.kktrkkt.studyolle.event;

import lombok.RequiredArgsConstructor;
import me.kktrkkt.studyolle.account.CurrentUser;
import me.kktrkkt.studyolle.account.entity.Account;
import me.kktrkkt.studyolle.study.Study;
import me.kktrkkt.studyolle.study.StudyService;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;
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
    static final String EVENT_VIEW = "event/view";
    static final String UPDATE_EVENT_URL = EVENT_URL + "/edit";
    static final String UPDATE_EVENT_VIEW = "event/editEventForm";

    private final StudyService studyService;
    private final EventService eventService;
    private final EventRepository events;
    private final ModelMapper modelMapper;
    private final EventValidator eventValidator;

    @InitBinder("eventForm")
    public void validEventForm(WebDataBinder dataBinder){
        dataBinder.addValidators(eventValidator);
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

    @GetMapping(EVENT_URL)
    public String eventView(@PathVariable String path, @PathVariable Long id,
                            @CurrentUser Account account, Model model) {
        Study study = studyService.getStudyToMemberAndManager(path);
        if(!study.isMember(account) && !study.isManager(account)){
            throw new AccessDeniedException("해당 모임에 접근할 권한이 없습니다.");
        }
        model.addAttribute(study);
        model.addAttribute(events.findWithEnrollmentById(id).orElseThrow());
        return EVENT_VIEW;
    }

    @GetMapping(UPDATE_EVENT_URL)
    public String updateEventForm(@PathVariable String path, @CurrentUser Account account,
                                @PathVariable Long id, Model model) {
        Study study = studyService.getStudyToUpdateStatus(account, path);
        Event event = events.findWithEnrollmentById(id).orElseThrow();
        model.addAttribute(study);
        model.addAttribute(event);
        model.addAttribute("eventTypes", EventType.values());
        model.addAttribute(modelMapper.map(event, EventForm.class));
        return UPDATE_EVENT_VIEW;
    }

    @PostMapping(UPDATE_EVENT_URL)
    public String updateEvent(@PathVariable String path, @CurrentUser Account account,
                              @PathVariable Long id, @Valid EventForm eventForm,
                              Errors errors, Model model) {
        Study study = studyService.getStudyToUpdateStatus(account, path);
        Event event = events.findWithEnrollmentById(id).orElseThrow();
        eventValidator.validateUpdateForm(eventForm, event, errors);
        if(errors.hasErrors()){
            model.addAttribute(study);
            model.addAttribute(event);
            model.addAttribute("eventTypes", EventType.values());
            return UPDATE_EVENT_VIEW;
        }
        eventService.update(eventForm, event);
        return "redirect:" + UPDATE_EVENT_URL;
    }
}
