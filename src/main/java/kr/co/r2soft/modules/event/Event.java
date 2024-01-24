package kr.co.r2soft.modules.event;

import lombok.Getter;
import lombok.Setter;
import kr.co.r2soft.modules.account.entity.Account;
import kr.co.r2soft.infra.entity.BaseEntity;
import kr.co.r2soft.modules.study.Study;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Getter @Setter
public class Event extends BaseEntity<Event> {

    @ManyToOne
    private Account createdBy;

    @ManyToOne
    private Study study;

    @OneToMany(mappedBy = "event", cascade = CascadeType.REMOVE)
    @OrderBy("enrolledAt")
    private Set<Enrollment> enrollments = new HashSet<>();

    @Column(nullable = false)
    private String title;

    @Lob
    private String description;

    private int limitOfEnrollments;

    @Column(nullable = false)
    private LocalDateTime createdDateTime;

    @Column(nullable = false)
    private LocalDateTime endEnrollmentDateTime;

    @Column(nullable = false)
    private LocalDateTime startDateTime;

    @Column(nullable = false)
    private LocalDateTime endDateTime;

    @Enumerated(EnumType.ORDINAL)
    private EventType eventType;

    public boolean isNew() {
        return !this.endDateTime.isBefore(LocalDateTime.now());
    }

    public boolean isOld() {
        return !isNew();
    }

    public boolean isEnrollable(Account account) {
        return !isEnrollDateTimeEnd() && !isEnrolled(account);
    }

    public boolean isEnrollDateTimeEnd() {
        return this.endEnrollmentDateTime.isBefore(LocalDateTime.now());
    }

    public boolean isEnrolled(Account account) {
        return enrollments.stream().anyMatch(x -> x.getAccount().equals(account));
    }

    public boolean isAttended(Account account) {
        return enrollments.stream().anyMatch(x -> x.getAccount().equals(account) && x.isAttended());
    }

    public Enrollment newEnrollment(Account account) {
        if(isEnrollable(account)){
            Enrollment enrollment = new Enrollment();
            enrollment.setEvent(this);
            enrollment.setEnrolledAt(LocalDateTime.now());
            enrollment.setAccount(account);
            updateAccept(enrollment);
            this.enrollments.add(enrollment);
            return enrollment;
        }
        else {
            throw new RuntimeException("You cannot enroll the event. Application for participation is closed or you have already enrolled");
        }
    }

    public Enrollment cancelEnrollment(Account account) {
        Enrollment enrollment = findEnrollment(account);
        this.enrollments.remove(enrollment);
        updateEnrollmentsStatus();
        return enrollment;
    }

    public Enrollment findEnrollment(Account account) {
        return enrollments.stream().filter(x -> x.getAccount().equals(account)).findFirst().orElseThrow(RuntimeException::new);
    }

    public void updateEnrollmentsStatus() {
        this.enrollments.forEach(this::updateAccept);
    }

    public void updateAccept(Enrollment enrollment) {
        if(!enrollment.isAccepted()){
            enrollment.setAccepted(this.eventType.equals(EventType.FCFS) && this.limitOfEnrollments > acceptCount());
        }
    }

    public long acceptCount() {
        return this.enrollments.stream().filter(Enrollment::isAccepted).count();
    }

    public boolean isOwner(Account account) {
        return this.createdBy.equals(account);
    }
}
