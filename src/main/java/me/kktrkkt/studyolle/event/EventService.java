package me.kktrkkt.studyolle.event;

import lombok.RequiredArgsConstructor;
import me.kktrkkt.studyolle.account.entity.Account;
import me.kktrkkt.studyolle.study.Study;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
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
        events.save(event);
    }

    public void delete(Long id) {
        events.deleteById(id);
    }

    public void join(Event event, Account account) {
        if(!event.isJoinable(account)){
            throw new IllegalStateException("You have already joined the event");
        }
        Enrollment enrollment = new Enrollment();
        enrollment.setEvent(event);
        enrollment.setAccepted(EventType.FCFS.equals(event.getEventType()) && event.getLimitOfEnrollments() >= event.getEnrollments().size());
        enrollment.setEnrolledAt(LocalDateTime.now());
        enrollment.setAccount(account);
        enrollments.save(enrollment);
    }
}
