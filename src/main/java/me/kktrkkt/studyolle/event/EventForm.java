package me.kktrkkt.studyolle.event;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
public class EventForm {

    @Size(max = 50)
    private String title;

    private EventType eventType;

    @Min(2)
    private int limitOfEnrollments;

    private LocalDateTime endEnrollmentDateTime;

    private LocalDateTime startDateTime;

    private LocalDateTime endDateTime;

    private String description;
}
