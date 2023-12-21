package me.kktrkkt.studyolle.event;

import me.kktrkkt.studyolle.account.WithAccount;
import me.kktrkkt.studyolle.study.Study;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;

import static java.time.LocalDateTime.now;
import static me.kktrkkt.studyolle.event.EventController.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
class EventControllerTest extends EventBaseTest {

    @Autowired
    private EventService eventService;

    @Autowired
    private EnrollmentRepository enrollments;

    @DisplayName("모임 생성 폼")
    @Test
    @WithAccount("user1")
    void newEventForm() throws Exception {
        Study study = createStudy("user1");

        this.mockMvc.perform(get(replacePath(study.getPath(), NEW_EVENT_URL)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name(NEW_EVENT_VIEW))
                .andExpect(model().attributeExists("study"))
                .andExpect(model().attributeExists("eventForm"))
                .andExpect(model().attributeExists("eventTypes"));
    }

    @DisplayName("모임 생성 - 성공")
    @Test
    @WithAccount("user1")
    void createNewEvent_success() throws Exception {
        Study study = createStudy("user1");
        String title = "title";
        EventType eventType = EventType.FCFS;
        int limitOfEnrollments = 5;
        LocalDateTime endDateTime = now().plus(3, ChronoUnit.DAYS);
        LocalDateTime endEnrollmentDateTime = now().plus(1, ChronoUnit.DAYS);
        LocalDateTime startDateTime = now().plus(2, ChronoUnit.DAYS);
        String description = "description";

        String studyBaseUrl = replacePath(study.getPath(), NEW_EVENT_URL);

        this.mockMvc.perform(post(studyBaseUrl).with(csrf())
                        .param("title", title)
                        .param("eventType", eventType.name())
                        .param("limitOfEnrollments", String.valueOf(limitOfEnrollments))
                        .param("endDateTime", endDateTime.format(DateTimeFormatter.ISO_DATE_TIME))
                        .param("endEnrollmentDateTime", endEnrollmentDateTime.format(DateTimeFormatter.ISO_DATE_TIME))
                        .param("startDateTime", startDateTime.format(DateTimeFormatter.ISO_DATE_TIME))
                        .param("description", description)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern(EVENT_URL));

        assertFalse(events.findAll().isEmpty());
    }

    @DisplayName("모임 생성 - 실패")
    @Test
    @WithAccount("user1")
    void createNewEvent_failure() throws Exception {
        Study study = createStudy("user1");

        requestWrongNewEvent("abcdeabcdeabcdeabcdeabcdeabcdeabcdeabcdeabcdeabcdea", EventType.FCFS.name(),
                2, now().plus(3, ChronoUnit.DAYS),
                now().plus(1, ChronoUnit.DAYS),
                now().plus(2, ChronoUnit.DAYS), "description", replacePath(study.getPath(), NEW_EVENT_URL));

        requestWrongNewEvent("title", "nope", 2,
                now().plus(3, ChronoUnit.DAYS), now().plus(1, ChronoUnit.DAYS),
                now().plus(2, ChronoUnit.DAYS), "description", replacePath(study.getPath(), NEW_EVENT_URL));

        requestWrongNewEvent("title", EventType.FCFS.name(), 1,
                now().plus(3, ChronoUnit.DAYS), now().plus(1, ChronoUnit.DAYS),
                now().plus(2, ChronoUnit.DAYS), "description", replacePath(study.getPath(), NEW_EVENT_URL));

        requestWrongNewEvent("title", EventType.FCFS.name(), 2,
                now().plus(3, ChronoUnit.DAYS), now(),
                now().plus(2, ChronoUnit.DAYS), "description", replacePath(study.getPath(), NEW_EVENT_URL));

        requestWrongNewEvent("title", EventType.FCFS.name(), 2,
                now().plus(2, ChronoUnit.DAYS), now().plus(1, ChronoUnit.DAYS),
                now(), "description", replacePath(study.getPath(), NEW_EVENT_URL));

        requestWrongNewEvent("title", EventType.FCFS.name(), 1,
                now().plus(2, ChronoUnit.DAYS), now().plus(1, ChronoUnit.DAYS),
                now().plus(2, ChronoUnit.DAYS), "description", replacePath(study.getPath(), NEW_EVENT_URL));

        assertTrue(events.findAll().isEmpty());
    }

    @DisplayName("모임 조회")
    @Test
    @WithAccount("user1")
    void eventView() throws Exception {
        Study study = createStudy("user1");
        Event event = createEvent("user1", study);

        this.mockMvc.perform(get(replacePathAndId(EVENT_URL, study.getPath(), event.getId())))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name(EVENT_VIEW))
                .andExpect(model().attributeExists("study"))
                .andExpect(model().attributeExists("event"));
    }

    @DisplayName("모임 수정 폼")
    @Test
    @WithAccount("user1")
    void eventUpdateForm() throws Exception {
        Study study = createStudy("user1");
        Event event = createEvent("user1", study);

        this.mockMvc.perform(get(replacePathAndId(EVENT_UPDATE_URL, study.getPath(), event.getId())))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name(EVENT_UPDATE_VIEW))
                .andExpect(model().attributeExists("study"))
                .andExpect(model().attributeExists("event"))
                .andExpect(model().attributeExists("eventTypes"))
                .andExpect(model().attributeExists("eventForm"));
    }

    @DisplayName("모임 수정 - 성공")
    @Test
    @WithAccount("user1")
    void updateEvent_success() throws Exception {
        Study study = createStudy("user1");
        Event event = createEvent("user1", study);

        String title = "new title";
        EventType eventType = EventType.CONFIRMATIVE;
        int limitOfEnrollments = 10;
        LocalDateTime endDateTime = now().plus(4, ChronoUnit.DAYS);
        LocalDateTime endEnrollmentDateTime = now().plus(2, ChronoUnit.DAYS);
        LocalDateTime startDateTime = now().plus(3, ChronoUnit.DAYS);
        String description = "new description";

        this.mockMvc.perform(post(replacePathAndId(EVENT_UPDATE_URL, study.getPath(), event.getId())).with(csrf())
                        .param("title", title)
                        .param("eventType", eventType.name())
                        .param("limitOfEnrollments", String.valueOf(limitOfEnrollments))
                        .param("endDateTime", endDateTime.format(DateTimeFormatter.ISO_DATE_TIME))
                        .param("endEnrollmentDateTime", endEnrollmentDateTime.format(DateTimeFormatter.ISO_DATE_TIME))
                        .param("startDateTime", startDateTime.format(DateTimeFormatter.ISO_DATE_TIME))
                        .param("description", description)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("success"))
                .andExpect(redirectedUrlPattern(EVENT_UPDATE_URL));

        assertEquals(title, event.getTitle());
        assertEquals(eventType, event.getEventType());
        assertEquals(limitOfEnrollments, event.getLimitOfEnrollments());
        assertEquals(endDateTime, event.getEndDateTime());
        assertEquals(endEnrollmentDateTime, event.getEndEnrollmentDateTime());
        assertEquals(startDateTime, event.getStartDateTime());
        assertEquals(description, event.getDescription());
    }

    @DisplayName("모임 수정 - 실패")
    @Test
    @WithAccount("user1")
    void updateEvent_failure() throws Exception {
        Study study = createStudy("user1");
        Event event = createEvent("user1", study);
        event.getEnrollments().addAll(Arrays.asList(createEnrollment(event), createEnrollment(event), createEnrollment(event)));
        events.save(event);

        String title = "new title";
        EventType eventType = EventType.CONFIRMATIVE;
        int limitOfEnrollments = 2;
        LocalDateTime endDateTime = now().plus(4, ChronoUnit.DAYS);
        LocalDateTime endEnrollmentDateTime = now().plus(2, ChronoUnit.DAYS);
        LocalDateTime startDateTime = now().plus(3, ChronoUnit.DAYS);
        String description = "new description";

        this.mockMvc.perform(post(replacePathAndId(EVENT_UPDATE_URL, study.getPath(), event.getId())).with(csrf())
                        .param("title", title)
                        .param("eventType", eventType.name())
                        .param("limitOfEnrollments", String.valueOf(limitOfEnrollments))
                        .param("endDateTime", endDateTime.format(DateTimeFormatter.ISO_DATE_TIME))
                        .param("endEnrollmentDateTime", endEnrollmentDateTime.format(DateTimeFormatter.ISO_DATE_TIME))
                        .param("startDateTime", startDateTime.format(DateTimeFormatter.ISO_DATE_TIME))
                        .param("description", description)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name(EVENT_UPDATE_VIEW))
                .andExpect(model().attributeExists("study"))
                .andExpect(model().attributeExists("event"))
                .andExpect(model().attributeExists("eventTypes"))
                .andExpect(model().attributeExists("eventForm"))
                .andExpect(model().hasErrors());

        assertNotEquals(title, event.getTitle());
        assertNotEquals(eventType, event.getEventType());
        assertNotEquals(limitOfEnrollments, event.getLimitOfEnrollments());
        assertNotEquals(endDateTime, event.getEndDateTime());
        assertNotEquals(endEnrollmentDateTime, event.getEndEnrollmentDateTime());
        assertNotEquals(startDateTime, event.getStartDateTime());
        assertNotEquals(description, event.getDescription());
    }

    @DisplayName("모임 삭제")
    @Test
    @WithAccount("user1")
    void deleteEvent() throws Exception {
        Study study = createStudy("user1");
        Event event = createEvent("user1", study);
        event.getEnrollments().addAll(Arrays.asList(createEnrollment(event), createEnrollment(event), createEnrollment(event)));
        events.save(event);

        this.mockMvc.perform(post(replacePathAndId(EVENT_DELETE_URL, study.getPath(), event.getId())).with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern(BASE_URL+"/events"));

        assertTrue(events.findById(event.getId()).isEmpty());
        assertTrue(enrollments.findAll().isEmpty());
    }

    private Enrollment createEnrollment(Event event) {
        Enrollment enrollment = new Enrollment();
        enrollment.setEvent(event);
        return enrollments.save(enrollment);
    }

    private Event createEvent(String nickname, Study study) {
        EventForm eventForm = new EventForm();
        eventForm.setTitle("event");
        eventForm.setEventType(EventType.FCFS);
        eventForm.setDescription("description");
        eventForm.setEndEnrollmentDateTime(now().plus(1, ChronoUnit.DAYS));
        eventForm.setStartDateTime(now().plus(2, ChronoUnit.DAYS));
        eventForm.setEndDateTime(now().plus(3, ChronoUnit.DAYS));
        eventForm.setLimitOfEnrollments(5);
        return eventService.create(accounts.findByNickname(nickname).orElseThrow(), eventForm, study);
    }

    private void requestWrongNewEvent(String title, String eventType, int limitOfEnrollments, LocalDateTime endDateTime, LocalDateTime endEnrollmentDateTime, LocalDateTime startDateTime, String description, String studyBaseUrl) throws Exception {
        this.mockMvc.perform(post(studyBaseUrl).with(csrf())
                        .param("title", title)
                        .param("eventType", String.valueOf(eventType))
                        .param("limitOfEnrollments", String.valueOf(limitOfEnrollments))
                        .param("endDateTime", endDateTime.format(DateTimeFormatter.ISO_DATE_TIME))
                        .param("endEnrollmentDateTime", endEnrollmentDateTime.format(DateTimeFormatter.ISO_DATE_TIME))
                        .param("startDateTime", startDateTime.format(DateTimeFormatter.ISO_DATE_TIME))
                        .param("description", description)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name(NEW_EVENT_VIEW))
                .andExpect(model().attributeExists("study"))
                .andExpect(model().attributeExists("eventForm"))
                .andExpect(model().attributeExists("eventTypes"))
                .andExpect(model().hasErrors());
    }

    private String replacePathAndId(String eventUrl, String path, Long id) {
        return eventUrl.replace("{path}", path).replace("{id}", String.valueOf(id));
    }
}