package me.kktrkkt.studyolle.event;

import lombok.Getter;
import lombok.Setter;
import me.kktrkkt.studyolle.account.entity.Account;
import me.kktrkkt.studyolle.infra.entity.BaseEntity;
import me.kktrkkt.studyolle.study.Study;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter @Setter
public class Event extends BaseEntity<Event> {

    @ManyToOne
    private Account createBy;

    @ManyToOne
    private Study study;

    @OneToMany(mappedBy = "event")
    private List<Enrollment> enrollments;

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
}
