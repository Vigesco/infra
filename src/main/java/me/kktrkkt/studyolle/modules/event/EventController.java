package me.kktrkkt.studyolle.modules.event;

import lombok.RequiredArgsConstructor;
import me.kktrkkt.studyolle.modules.account.CurrentUser;
import me.kktrkkt.studyolle.modules.account.entity.Account;
import me.kktrkkt.studyolle.modules.study.Study;
import me.kktrkkt.studyolle.modules.study.StudyService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class EventController {

    static final String BASE_URL = "/study/{path}";
    static final String NEW_EVENT_URL = BASE_URL + "/new-event";
    static final String NEW_EVENT_VIEW = "event/newEventForm";
    static final String EVENTS_URL = BASE_URL + "/events";
    static final String EVENTS_VIEW = "study/events";
    static final String EVENT_URL = BASE_URL + "/event/{id}";
    static final String EVENT_VIEW = "event/view";
    static final String EVENT_UPDATE_URL = EVENT_URL + "/edit";
    static final String EVENT_UPDATE_VIEW = "event/eventUpdateForm";
    static final String EVENT_DELETE_URL = EVENT_URL + "/delete";
    static final String EVENT_ENROLL_URL = EVENT_URL + "/enroll";
    static final String EVENT_DISENROLL_URL = EVENT_URL + "/disenroll";
    static final String EVENT_ENROLLMENT_URL = EVENT_URL + "/enrollment/{enrollmentId}";
    static final String EVENT_ENROLLMENT_ACCEPT_URL = EVENT_ENROLLMENT_URL + "/accept";
    static final String EVENT_ENROLLMENT_REJECT_URL = EVENT_ENROLLMENT_URL + "/reject";
    static final String EVENT_ENROLLMENT_CHECK_IN_URL = EVENT_ENROLLMENT_URL + "/check-in";
    static final String EVENT_ENROLLMENT_CANCEL_CHECK_IN_URL = EVENT_ENROLLMENT_URL + "/cancel-check-in";

    private final StudyService studyService;
    private final EventService eventService;
    private final EventRepository events;
    private final ModelMapper modelMapper;
    private final EventValidator eventValidator;

    @InitBinder("eventForm")
    public void validEventForm(WebDataBinder dataBinder){
        dataBinder.addValidators(eventValidator);
    }

    @ModelAttribute
    public void validStudyEventOwner(@PathVariable String path, @PathVariable(value="id", required = false) Event event){
        if(event != null && !event.getStudy().getPath().equals(path)){
            throw new IllegalArgumentException();
        }
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

    @GetMapping(EVENTS_URL)
    public String eventList(@PathVariable String path, @CurrentUser Account account, Model model) {
        Study study = studyService.getStudyToEvent(path, account);
        List<Event> eventList = events.findByStudyOrderByCreatedDateTimeDesc(study);

        model.addAttribute(study);

        List<Event> newEvents = new ArrayList<>();
        List<Event> oldEvents = new ArrayList<>();
        eventList.forEach(e -> {
            if (e.isOld()) {
                oldEvents.add(e);
            }
            else if (e.isNew()) {
                newEvents.add(e);
            }
        });

        model.addAttribute("newEvents", newEvents);
        model.addAttribute("oldEvents", oldEvents);
        return EVENTS_VIEW;
    }

    @GetMapping(EVENT_URL)
    public String eventView(@PathVariable String path, @PathVariable Long id,
                            @CurrentUser Account account, Model model) {
        Study study = studyService.getStudyToMemberAndManager(path, account);
        model.addAttribute(study);
        model.addAttribute(events.findWithEnrollmentById(id).orElseThrow());
        return EVENT_VIEW;
    }

    @GetMapping(EVENT_UPDATE_URL)
    public String updateEventForm(@PathVariable String path, @CurrentUser Account account,
                                @PathVariable Long id, Model model) {
        Study study = studyService.getStudyToUpdateStatus(account, path);
        Event event = events.findWithEnrollmentById(id).orElseThrow();
        model.addAttribute(study);
        model.addAttribute(event);
        model.addAttribute("eventTypes", EventType.values());
        model.addAttribute(modelMapper.map(event, EventForm.class));
        return EVENT_UPDATE_VIEW;
    }

    @PostMapping(EVENT_UPDATE_URL)
    public String updateEvent(@PathVariable String path, @CurrentUser Account account,
                              @PathVariable Long id, @Valid EventForm eventForm,
                              Errors errors, Model model,
                              RedirectAttributes ra) {
        Study study = studyService.getStudyToUpdateStatus(account, path);
        Event event = events.findWithEnrollmentById(id).orElseThrow();
        eventValidator.validateUpdateForm(eventForm, event, errors);
        if(errors.hasErrors()){
            model.addAttribute(study);
            model.addAttribute(event);
            model.addAttribute("eventTypes", EventType.values());
            return EVENT_UPDATE_VIEW;
        }
        eventService.update(eventForm, event);
        ra.addFlashAttribute("success", "success");
        return "redirect:" + EVENT_UPDATE_URL;
    }

    @PostMapping(EVENT_ENROLL_URL)
    public String newEnrollment(@PathVariable String path, @CurrentUser Account account,
                              @PathVariable Long id, RedirectAttributes ra) {
        studyService.getStudyToMemberAndManager(path, account);
        Event event = events.findById(id).orElseThrow();
        eventService.enroll(event, account);
        ra.addFlashAttribute("success", "success.enroll");
        return "redirect:" + EVENT_URL;
    }

    @PostMapping(EVENT_DISENROLL_URL)
    public String cancelEnrollment(@CurrentUser Account account, @PathVariable Long id,
                              RedirectAttributes ra) {
        Event event = events.findWithEnrollmentById(id).orElseThrow();
        eventService.cancelEnrollment(event, account);
        ra.addFlashAttribute("success", "success.cancel");
        return "redirect:" + EVENT_URL;
    }

    @PostMapping(EVENT_DELETE_URL)
    public String deleteEvent(@PathVariable String path, @CurrentUser Account account,
                              @PathVariable Long id) {
        studyService.getStudyToUpdateStatus(account, path);
        eventService.delete(id);
        return "redirect:" + BASE_URL + "/events";
    }

    @PostMapping(EVENT_ENROLLMENT_ACCEPT_URL)
    public String acceptEnrollment(@PathVariable String path, @CurrentUser Account account,
                                   @PathVariable("enrollmentId") Enrollment enrollment) {
        studyService.getStudyToUpdateStatus(account, path);
        eventService.accept(enrollment);
        return "redirect:" + EVENT_URL;
    }

    @PostMapping(EVENT_ENROLLMENT_REJECT_URL)
    public String rejectEnrollment(@PathVariable String path, @CurrentUser Account account,
                                   @PathVariable("enrollmentId") Enrollment enrollment) {
        studyService.getStudyToUpdateStatus(account, path);
        eventService.reject(enrollment);
        return "redirect:" + EVENT_URL;
    }

    @PostMapping(EVENT_ENROLLMENT_CHECK_IN_URL)
    public String checkInEnrollment(@PathVariable String path, @CurrentUser Account account,
                                   @PathVariable("enrollmentId") Enrollment enrollment) {
        studyService.getStudyToUpdateStatus(account, path);
        eventService.checkIn(enrollment);
        return "redirect:" + EVENT_URL;
    }

    @PostMapping(EVENT_ENROLLMENT_CANCEL_CHECK_IN_URL)
    public String cancelCheckInEnrollment(@PathVariable String path, @CurrentUser Account account,
                                   @PathVariable("enrollmentId") Enrollment enrollment) {
        studyService.getStudyToUpdateStatus(account, path);
        eventService.cancelCheckIn(enrollment);
        return "redirect:" + EVENT_URL;
    }
}
