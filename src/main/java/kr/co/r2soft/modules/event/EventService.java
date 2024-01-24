package kr.co.r2soft.modules.event;

import lombok.RequiredArgsConstructor;
import kr.co.r2soft.modules.account.entity.Account;
import kr.co.r2soft.modules.event.event.EnrollmentEvent;
import kr.co.r2soft.modules.study.Study;
import kr.co.r2soft.modules.study.event.StudyUpdatedEvent;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
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

    private final ApplicationEventPublisher eventPublisher;

    public Event create(Account account, EventForm eventForm, Study study) {
        Event event = modelMapper.map(eventForm, Event.class);
        event.setCreatedBy(account);
        event.setCreatedDateTime(LocalDateTime.now());
        event.setStudy(study);
        Event save = events.save(event);
        eventPublisher.publishEvent(new StudyUpdatedEvent(study, "모임이 생겼습니다"));
        return save;
    }

    public void update(EventForm eventForm, Event event) {
        modelMapper.map(eventForm, event);
        event.updateEnrollmentsStatus();
        eventPublisher.publishEvent(new StudyUpdatedEvent(event.getStudy(), "모임을 수정했습니다."));
    }

    public void delete(Long id) {
        eventPublisher.publishEvent(new StudyUpdatedEvent(events.findById(id).orElseThrow().getStudy(), "모임이 취소되었습니다."));
        events.deleteById(id);
    }

    public void enroll(Event event, Account account) {
        enrollments.save(event.newEnrollment(account));
    }

    public void cancelEnrollment(Event event, Account account) {
        enrollments.delete(event.cancelEnrollment(account));
    }

    public void accept(Enrollment enrollment) {
        enrollment.accept();
        eventPublisher.publishEvent(new EnrollmentEvent(enrollment, "모임 참가 신청이 수락되었습니다."));
    }

    public void reject(Enrollment enrollment) {
        enrollment.reject();
        eventPublisher.publishEvent(new EnrollmentEvent(enrollment, "모임 참가 신청이 거절되었습니다."));
    }

    public void checkIn(Enrollment enrollment) {
        enrollment.attend();
    }

    public void cancelCheckIn(Enrollment enrollment) {
        enrollment.cancelAttend();
    }
}
