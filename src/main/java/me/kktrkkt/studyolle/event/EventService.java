package me.kktrkkt.studyolle.event;

import lombok.RequiredArgsConstructor;
import me.kktrkkt.studyolle.account.entity.Account;
import me.kktrkkt.studyolle.study.Study;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class EventService {

    private final ModelMapper modelMapper;

    private final EventRepository events;

    private final EnrollmentRepository enrollments;

    public Event create(Account account, EventForm eventForm, Study study) {
        Event event = modelMapper.map(eventForm, Event.class);
        event.setCreatedBy(account);
        event.setCreatedDateTime(LocalDateTime.now());
        event.setStudy(study);
        return events.save(event);
    }

    public void update(EventForm eventForm, Event event) {
        modelMapper.map(eventForm, event);
        event.updateEnrollmentsStatus();
    }

    public void delete(Long id) {
        events.deleteById(id);
    }

    public void enroll(Event event, Account account) {
        enrollments.save(event.newEnrollment(account));
    }

    public void cancelEnrollment(Event event, Account account) {
        enrollments.delete(event.cancelEnrollment(account));
    }
}
