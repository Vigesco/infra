package me.kktrkkt.studyolle.event;

import me.kktrkkt.studyolle.account.WithAccount;
import me.kktrkkt.studyolle.study.Study;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import static java.time.LocalDateTime.now;
import static me.kktrkkt.studyolle.event.EventController.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
class EventControllerTest extends EventBaseTest {

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
        LocalDateTime endEnrollmentDateTime = now().plus(2, ChronoUnit.DAYS);
        LocalDateTime startDateTime = now().plus(1, ChronoUnit.DAYS);
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
                now().plus(2, ChronoUnit.DAYS),
                now().plus(1, ChronoUnit.DAYS), "description", replacePath(study.getPath(), NEW_EVENT_URL));

        requestWrongNewEvent("title", "nope", 2,
                now().plus(3, ChronoUnit.DAYS), now().plus(2, ChronoUnit.DAYS),
                now().plus(1, ChronoUnit.DAYS), "description", replacePath(study.getPath(), NEW_EVENT_URL));

        requestWrongNewEvent("title", EventType.FCFS.name(), 1,
                now().plus(3, ChronoUnit.DAYS), now().plus(2, ChronoUnit.DAYS),
                now().plus(1, ChronoUnit.DAYS), "description", replacePath(study.getPath(), NEW_EVENT_URL));

        requestWrongNewEvent("title", EventType.FCFS.name(), 2,
                now().plus(3, ChronoUnit.DAYS), now().plus(2, ChronoUnit.DAYS),
                now(), "description", replacePath(study.getPath(), NEW_EVENT_URL));

        requestWrongNewEvent("title", EventType.FCFS.name(), 2,
                now().plus(3, ChronoUnit.DAYS), now().plus(2, ChronoUnit.DAYS),
                now().plus(4, ChronoUnit.DAYS), "description", replacePath(study.getPath(), NEW_EVENT_URL));

        requestWrongNewEvent("title", EventType.FCFS.name(), 1,
                now().plus(3, ChronoUnit.DAYS), now().plus(1, ChronoUnit.DAYS),
                now().plus(1, ChronoUnit.DAYS), "description", replacePath(study.getPath(), NEW_EVENT_URL));

        requestWrongNewEvent("title", EventType.FCFS.name(), 1,
                now().plus(3, ChronoUnit.DAYS), now().plus(3, ChronoUnit.DAYS),
                now().plus(1, ChronoUnit.DAYS), "description", replacePath(study.getPath(), NEW_EVENT_URL));

        assertTrue(events.findAll().isEmpty());
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