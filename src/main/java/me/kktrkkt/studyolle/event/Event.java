package me.kktrkkt.studyolle.event;

import lombok.Getter;
import lombok.Setter;
import me.kktrkkt.studyolle.account.entity.Account;
import me.kktrkkt.studyolle.infra.entity.BaseEntity;
import me.kktrkkt.studyolle.study.Study;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter @Setter
public class Event extends BaseEntity<Event> {

    @ManyToOne
    private Account createdBy;

    @ManyToOne
    private Study study;

    @OneToMany(mappedBy = "event")
    private Set<Enrollment> enrollments = new HashSet<>();

    @Column(nullable = false)
    private String title;

    @Lob
    private String description;

    private Integer limitOfEnrollments;

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
}
