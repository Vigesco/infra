package kr.co.r2soft.modules.event;

import kr.co.r2soft.infra.MockMvcTest;
import kr.co.r2soft.modules.account.AccountRepository;
import kr.co.r2soft.modules.account.WithAccount;
import kr.co.r2soft.modules.account.entity.Account;
import kr.co.r2soft.modules.event.event.EnrollmentEvent;
import kr.co.r2soft.modules.event.event.EventEventListener;
import kr.co.r2soft.modules.study.Study;
import kr.co.r2soft.modules.study.StudyFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;

import static java.time.LocalDateTime.now;
import static kr.co.r2soft.infra.Utils.*;
import static kr.co.r2soft.modules.event.EventController.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@MockMvcTest
@Transactional
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private StudyFactory studyFactory;

    @Autowired
    private AccountRepository accounts;

    @Autowired
    private EventRepository events;

    @Autowired
    private EventService eventService;

    @Autowired
    private EnrollmentRepository enrollments;

    @MockBean
    private EventEventListener EventEventListener;

    @DisplayName("모임 생성 폼")
    @Test
    @WithAccount("user1")
    void newEventForm() throws Exception {
        Study study = studyFactory.createStudy("user1");

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
        Study study = studyFactory.createStudy("user1");
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
        Study study = studyFactory.createStudy("user1");

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

    @DisplayName("모임 목록 조회")
    @Test
    @WithAccount("user1")
    void studyEvents() throws Exception {
        Study study = studyFactory.createStudy("user1");

        this.mockMvc.perform(get(replacePath(study.getPath(), EVENTS_URL)))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("study"))
                .andExpect(model().attributeExists("newEvents"))
                .andExpect(model().attributeExists("oldEvents"))
                .andExpect(view().name(EVENTS_VIEW))
                .andDo(print());
    }

    @DisplayName("모임 조회")
    @Test
    @WithAccount("user1")
    void eventView() throws Exception {
        Study study = studyFactory.createStudy("user1");
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
        Study study = studyFactory.createStudy("user1");
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
        Study study = studyFactory.createStudy("user1");
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
    @WithAccount({"user1", "user2"})
    void updateEvent_failure() throws Exception {
        Study study = studyFactory.createStudy("user1");
        study.publish();
        study.startRecruiting();
        Event event = createEvent("user1", study);
        event.getEnrollments().addAll(Arrays.asList(createEnrollment(event, true),
                createEnrollment(event, true), createEnrollment(event, true)));
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

    @DisplayName("모임 참가 신청 - 자동수락")
    @Test
    @WithAccount({"user1", "user2", "user3"})
    void enrollEvent_fcfs() throws Exception {
        Study study = studyFactory.createStudy("user1");
        study.publish();
        study.startRecruiting();
        Event event = createEvent("user1", study);
        event.setLimitOfEnrollments(2);
        Account user2 = accounts.findByNickname("user2").orElseThrow();
        Account user3 = accounts.findByNickname("user3").orElseThrow();
        study.addMember(user2);
        study.addMember(user3);
        Enrollment user2Enrollment = event.newEnrollment(user2);
        Enrollment user3Enrollment = event.newEnrollment(user3);
        enrollments.save(user3Enrollment);
        enrollments.save(user2Enrollment);

        this.mockMvc.perform(post(replacePathAndId(EVENT_ENROLL_URL, study.getPath(), event.getId())).with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("success", "success.enroll"))
                .andExpect(redirectedUrlPattern(EVENT_URL));

        assertEquals(3, event.getEnrollments().size());
        assertTrue(user2Enrollment.isAccepted());
        assertTrue(user3Enrollment.isAccepted());
        Enrollment user1Enrollment = event.getEnrollments().stream()
                .filter(x -> x.getAccount().getNickname().equals("user1"))
                .findFirst()
                .orElseThrow();
        assertFalse(user1Enrollment.isAccepted());
    }

    @DisplayName("모임 참가 신청 - 관리자확인")
    @Test
    @WithAccount({"user1", "user2", "user3"})
    void enrollEvent_confirm() throws Exception {
        Study study = studyFactory.createStudy("user1");
        study.publish();
        study.startRecruiting();
        Event event = createEvent("user1", study);
        event.setLimitOfEnrollments(2);
        event.setEventType(EventType.CONFIRMATIVE);
        Account user2 = accounts.findByNickname("user2").orElseThrow();
        Account user3 = accounts.findByNickname("user3").orElseThrow();
        study.addMember(user2);
        study.addMember(user3);
        Enrollment user2Enrollment = event.newEnrollment(user2);
        Enrollment user3Enrollment = event.newEnrollment(user3);
        enrollments.save(user3Enrollment);
        enrollments.save(user2Enrollment);

        this.mockMvc.perform(post(replacePathAndId(EVENT_ENROLL_URL, study.getPath(), event.getId())).with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("success", "success.enroll"))
                .andExpect(redirectedUrlPattern(EVENT_URL));

        assertEquals(3, event.getEnrollments().size());
        assertFalse(user2Enrollment.isAccepted());
        assertFalse(user3Enrollment.isAccepted());
        Enrollment user1Enrollment = event.getEnrollments().stream()
                .filter(x -> x.getAccount().getNickname().equals("user1"))
                .findFirst()
                .orElseThrow();
        assertFalse(user1Enrollment.isAccepted());
    }

    @DisplayName("모임 참가 신청 - 실패")
    @Test
    @WithAccount({"user1"})
    void enrollEvent_failure() throws Exception {
        Study study = studyFactory.createStudy("user1");
        study.publish();
        study.startRecruiting();
        Event event = createEvent("user1", study);
        event.setLimitOfEnrollments(2);
        event.setEndEnrollmentDateTime(now());

        this.mockMvc.perform(post(replacePathAndId(EVENT_ENROLL_URL, study.getPath(), event.getId())).with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("error"));
        event.setEndEnrollmentDateTime(now().plus(1, ChronoUnit.DAYS));
        assertEquals(0, event.getEnrollments().size());

        Enrollment enrollment = event.newEnrollment(accounts.findByNickname("user1").orElseThrow());
        enrollments.save(enrollment);
        this.mockMvc.perform(post(replacePathAndId(EVENT_ENROLL_URL, study.getPath(), event.getId())).with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("error"));
        assertEquals(1, event.getEnrollments().size());
    }

    @DisplayName("모임 참가 신청취소 - 성공")
    @Test
    @WithAccount({"user1", "user2", "user3"})
    void cancelEnrollment_success() throws Exception {
        Study study = studyFactory.createStudy("user1");
        study.publish();
        study.startRecruiting();
        Event event = createEvent("user1", study);
        event.setLimitOfEnrollments(2);
        Account user1 = accounts.findByNickname("user1").orElseThrow();
        Account user2 = accounts.findByNickname("user2").orElseThrow();
        Account user3 = accounts.findByNickname("user3").orElseThrow();
        study.addMember(user2);
        study.addMember(user3);
        Enrollment user1Enrollment = event.newEnrollment(user1);
        Enrollment user2Enrollment = event.newEnrollment(user2);
        Enrollment user3Enrollment = event.newEnrollment(user3);
        enrollments.save(user1Enrollment);
        enrollments.save(user2Enrollment);
        enrollments.save(user3Enrollment);

        assertTrue(user1Enrollment.isAccepted());
        assertTrue(user2Enrollment.isAccepted());
        assertFalse(user3Enrollment.isAccepted());

        this.mockMvc.perform(post(replacePathAndId(EVENT_DISENROLL_URL, study.getPath(), event.getId())).with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("success", "success.cancel"))
                .andExpect(redirectedUrlPattern(EVENT_URL));

        assertEquals(2, event.getEnrollments().size());
        assertTrue(user2Enrollment.isAccepted());
        assertTrue(user3Enrollment.isAccepted());
    }

    @DisplayName("모임 참가 신청 수락 - 성공")
    @Test
    @WithAccount({"user1", "user2", "user3"})
    void acceptEnrollment_success() throws Exception {
        Study study = studyFactory.createStudy("user1");
        study.publish();
        study.startRecruiting();
        Event event = createEvent("user1", study);
        event.setEventType(EventType.CONFIRMATIVE);
        event.setLimitOfEnrollments(2);
        Account user2 = accounts.findByNickname("user2").orElseThrow();
        Account user3 = accounts.findByNickname("user3").orElseThrow();
        study.addMember(user2);
        study.addMember(user3);
        Enrollment user2Enrollment = event.newEnrollment(user2);
        Enrollment user3Enrollment = event.newEnrollment(user3);
        enrollments.save(user3Enrollment);
        enrollments.save(user2Enrollment);

        this.mockMvc.perform(post(replacePathAndIdAndEnrollmentId(EVENT_ENROLLMENT_ACCEPT_URL, study.getPath(), event.getId(), user2Enrollment.getId())).with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern(EVENT_URL));

        assertEquals(2, event.getEnrollments().size());
        assertTrue(user2Enrollment.isAccepted());
        assertFalse(user3Enrollment.isAccepted());

        then(EventEventListener).should().handleEnrollmentEvent(any(EnrollmentEvent.class));
    }

    @DisplayName("모임 참가 신청 수락 - 실패")
    @Test
    @WithAccount({"user1", "user2", "user3"})
    void acceptEnrollment_failure() throws Exception {
        Study study = studyFactory.createStudy("user1");
        study.publish();
        study.startRecruiting();
        Event event = createEvent("user1", study);
        event.setEventType(EventType.CONFIRMATIVE);
        event.setLimitOfEnrollments(2);
        Account user2 = accounts.findByNickname("user2").orElseThrow();
        Account user3 = accounts.findByNickname("user3").orElseThrow();
        study.addMember(user2);
        study.addMember(user3);
        Enrollment user2Enrollment = event.newEnrollment(user2);
        Enrollment user3Enrollment = event.newEnrollment(user3);
        user2Enrollment.accept();
        enrollments.save(user3Enrollment);
        enrollments.save(user2Enrollment);

        this.mockMvc.perform(post(replacePathAndIdAndEnrollmentId(EVENT_ENROLLMENT_ACCEPT_URL, study.getPath(), event.getId(), user2Enrollment.getId())).with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("error"));
    }

    @DisplayName("모임 참가 신청 취소 - 성공")
    @Test
    @WithAccount({"user1", "user2", "user3"})
    void rejectEnrollment_success() throws Exception {
        Study study = studyFactory.createStudy("user1");
        study.publish();
        study.startRecruiting();
        Event event = createEvent("user1", study);
        event.setEventType(EventType.CONFIRMATIVE);
        event.setLimitOfEnrollments(2);
        Account user2 = accounts.findByNickname("user2").orElseThrow();
        Account user3 = accounts.findByNickname("user3").orElseThrow();
        study.addMember(user2);
        study.addMember(user3);
        Enrollment user2Enrollment = event.newEnrollment(user2);
        Enrollment user3Enrollment = event.newEnrollment(user3);
        user2Enrollment.accept();
        enrollments.save(user3Enrollment);
        enrollments.save(user2Enrollment);

        this.mockMvc.perform(post(replacePathAndIdAndEnrollmentId(EVENT_ENROLLMENT_REJECT_URL, study.getPath(), event.getId(), user2Enrollment.getId())).with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern(EVENT_URL));

        assertFalse(user2Enrollment.isAccepted());
        assertFalse(user3Enrollment.isAccepted());

        then(EventEventListener).should().handleEnrollmentEvent(any(EnrollmentEvent.class));
    }

    @DisplayName("모임 참가 신청 취소 - 실패")
    @Test
    @WithAccount({"user1", "user2", "user3"})
    void rejectEnrollment_failure() throws Exception {
        Study study = studyFactory.createStudy("user1");
        study.publish();
        study.startRecruiting();
        Event event = createEvent("user1", study);
        event.setEventType(EventType.CONFIRMATIVE);
        event.setLimitOfEnrollments(2);
        Account user2 = accounts.findByNickname("user2").orElseThrow();
        Account user3 = accounts.findByNickname("user3").orElseThrow();
        study.addMember(user2);
        study.addMember(user3);
        Enrollment user2Enrollment = event.newEnrollment(user2);
        Enrollment user3Enrollment = event.newEnrollment(user3);
        enrollments.save(user3Enrollment);
        enrollments.save(user2Enrollment);

        this.mockMvc.perform(post(replacePathAndIdAndEnrollmentId(EVENT_ENROLLMENT_REJECT_URL, study.getPath(), event.getId(), user2Enrollment.getId())).with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("error"));
    }

    @DisplayName("모임 체크인 - 성공")
    @Test
    @WithAccount({"user1", "user2", "user3"})
    void checkInEvent_success() throws Exception {
        Study study = studyFactory.createStudy("user1");
        study.publish();
        study.startRecruiting();
        Event event = createEvent("user1", study);
        event.setEventType(EventType.CONFIRMATIVE);
        event.setLimitOfEnrollments(2);
        Account user2 = accounts.findByNickname("user2").orElseThrow();
        Account user3 = accounts.findByNickname("user3").orElseThrow();
        study.addMember(user2);
        study.addMember(user3);
        Enrollment user2Enrollment = event.newEnrollment(user2);
        Enrollment user3Enrollment = event.newEnrollment(user3);
        user2Enrollment.accept();
        enrollments.save(user3Enrollment);
        enrollments.save(user2Enrollment);

        this.mockMvc.perform(post(replacePathAndIdAndEnrollmentId(EVENT_ENROLLMENT_CHECK_IN_URL, study.getPath(), event.getId(), user2Enrollment.getId())).with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern(EVENT_URL));

        assertTrue(user2Enrollment.isAttended());
        assertFalse(user3Enrollment.isAttended());
    }

    @DisplayName("모임 체크인 - 실패")
    @Test
    @WithAccount({"user1", "user2", "user3"})
    void checkInEvent_failure() throws Exception {
        Study study = studyFactory.createStudy("user1");
        study.publish();
        study.startRecruiting();
        Event event = createEvent("user1", study);
        event.setEventType(EventType.CONFIRMATIVE);
        event.setLimitOfEnrollments(2);
        Account user2 = accounts.findByNickname("user2").orElseThrow();
        Account user3 = accounts.findByNickname("user3").orElseThrow();
        study.addMember(user2);
        study.addMember(user3);
        Enrollment user2Enrollment = event.newEnrollment(user2);
        Enrollment user3Enrollment = event.newEnrollment(user3);
        enrollments.save(user3Enrollment);
        enrollments.save(user2Enrollment);

        this.mockMvc.perform(post(replacePathAndIdAndEnrollmentId(EVENT_ENROLLMENT_CHECK_IN_URL, study.getPath(), event.getId(), user2Enrollment.getId())).with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("error"));

        user2Enrollment.accept();
        user2Enrollment.attend();

        this.mockMvc.perform(post(replacePathAndIdAndEnrollmentId(EVENT_ENROLLMENT_CHECK_IN_URL, study.getPath(), event.getId(), user2Enrollment.getId())).with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("error"));
    }

    @DisplayName("모임 체크인 취소 - 성공")
    @Test
    @WithAccount({"user1", "user2", "user3"})
    void cancelCheckInEvent_success() throws Exception {
        Study study = studyFactory.createStudy("user1");
        study.publish();
        study.startRecruiting();
        Event event = createEvent("user1", study);
        event.setEventType(EventType.CONFIRMATIVE);
        event.setLimitOfEnrollments(2);
        Account user2 = accounts.findByNickname("user2").orElseThrow();
        Account user3 = accounts.findByNickname("user3").orElseThrow();
        study.addMember(user2);
        study.addMember(user3);
        Enrollment user2Enrollment = event.newEnrollment(user2);
        Enrollment user3Enrollment = event.newEnrollment(user3);
        user2Enrollment.accept();
        user2Enrollment.attend();
        enrollments.save(user3Enrollment);
        enrollments.save(user2Enrollment);

        this.mockMvc.perform(post(replacePathAndIdAndEnrollmentId(EVENT_ENROLLMENT_CANCEL_CHECK_IN_URL, study.getPath(), event.getId(), user2Enrollment.getId())).with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern(EVENT_URL));

        assertFalse(user2Enrollment.isAttended());
        assertFalse(user3Enrollment.isAttended());
    }

    @DisplayName("모임 체크인 - 실패")
    @Test
    @WithAccount({"user1", "user2", "user3"})
    void cancelCheckInEvent_failure() throws Exception {
        Study study = studyFactory.createStudy("user1");
        study.publish();
        study.startRecruiting();
        Event event = createEvent("user1", study);
        event.setEventType(EventType.CONFIRMATIVE);
        event.setLimitOfEnrollments(2);
        Account user2 = accounts.findByNickname("user2").orElseThrow();
        Account user3 = accounts.findByNickname("user3").orElseThrow();
        study.addMember(user2);
        study.addMember(user3);
        Enrollment user2Enrollment = event.newEnrollment(user2);
        Enrollment user3Enrollment = event.newEnrollment(user3);
        user2Enrollment.accept();
        enrollments.save(user3Enrollment);
        enrollments.save(user2Enrollment);

        this.mockMvc.perform(post(replacePathAndIdAndEnrollmentId(EVENT_ENROLLMENT_CANCEL_CHECK_IN_URL, study.getPath(), event.getId(), user2Enrollment.getId())).with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("error"));
    }

    @DisplayName("다른 스터디의 주인이 남의 스터디의 모임을 수락")
    @Test
    @WithAccount({"user2", "user1", "user3"})
    void anotherStudyOwnerAcceptAnotherStudyEvent() throws Exception {
        Study study1 = studyFactory.createStudy("user1");
        study1.publish();
        study1.startRecruiting();
        Event event = createEvent("user1", study1);
        event.setEventType(EventType.CONFIRMATIVE);
        event.setLimitOfEnrollments(2);
        Account user2 = accounts.findByNickname("user2").orElseThrow();
        Account user3 = accounts.findByNickname("user3").orElseThrow();
        study1.addMember(user2);
        study1.addMember(user3);
        Enrollment user2Enrollment = event.newEnrollment(user2);
        Enrollment user3Enrollment = event.newEnrollment(user3);
        enrollments.save(user3Enrollment);
        enrollments.save(user2Enrollment);

        Study study2 = studyFactory.createStudy("user2", "study2");

        this.mockMvc.perform(post(replacePathAndIdAndEnrollmentId(EVENT_ENROLLMENT_ACCEPT_URL, study2.getPath(), event.getId(), user2Enrollment.getId())).with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("error"));
    }

    @DisplayName("모임 모집인원 수정후 참가자 신청상태 변경 - 성공")
    @Test
    @WithAccount({"user1", "user2", "user3"})
    void updateEventLimitOfEnrolmentAndUpdateEnrollmentAccept_success() throws Exception {
        Study study = studyFactory.createStudy("user1");
        study.publish();
        study.startRecruiting();
        Event event = createEvent("user1", study);
        event.setLimitOfEnrollments(2);
        Account user1 = accounts.findByNickname("user1").orElseThrow();
        Account user2 = accounts.findByNickname("user2").orElseThrow();
        Account user3 = accounts.findByNickname("user3").orElseThrow();
        study.addMember(user2);
        study.addMember(user3);
        Enrollment user1Enrollment = event.newEnrollment(user1);
        Enrollment user2Enrollment = event.newEnrollment(user2);
        Enrollment user3Enrollment = event.newEnrollment(user3);
        enrollments.save(user1Enrollment);
        enrollments.save(user2Enrollment);
        enrollments.save(user3Enrollment);

        assertTrue(user1Enrollment.isAccepted());
        assertTrue(user2Enrollment.isAccepted());
        assertFalse(user3Enrollment.isAccepted());

        this.mockMvc.perform(post(replacePathAndId(EVENT_UPDATE_URL, study.getPath(), event.getId())).with(csrf())
                        .param("title", event.getTitle())
                        .param("eventType", event.getEventType().name())
                        .param("limitOfEnrollments", "3")
                        .param("endDateTime", event.getEndDateTime().format(DateTimeFormatter.ISO_DATE_TIME))
                        .param("endEnrollmentDateTime", event.getEndEnrollmentDateTime().format(DateTimeFormatter.ISO_DATE_TIME))
                        .param("startDateTime", event.getStartDateTime().format(DateTimeFormatter.ISO_DATE_TIME))
                        .param("description", event.getDescription())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("success"))
                .andExpect(redirectedUrlPattern(EVENT_UPDATE_URL));

        assertTrue(user2Enrollment.isAccepted());
        assertTrue(user3Enrollment.isAccepted());
        assertTrue(user3Enrollment.isAccepted());
    }

    @DisplayName("모임 삭제")
    @Test
    @WithAccount("user1")
    void deleteEvent() throws Exception {
        Study study = studyFactory.createStudy("user1");
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

    private Enrollment createEnrollment(Event event, boolean accept) {
        Enrollment enrollment = createEnrollment(event);
        enrollment.setAccepted(accept);
        return enrollment;
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
}