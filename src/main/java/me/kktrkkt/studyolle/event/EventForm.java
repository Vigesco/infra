package me.kktrkkt.studyolle.event;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
public class EventForm {

    @NotEmpty
    @Size(max = 50)
    private String title;

    @NotNull
    private EventType eventType;

    @Min(2)
    private int limitOfEnrollments;

    @NotNull
    private LocalDateTime endEnrollmentDateTime;

    @NotNull
    private LocalDateTime startDateTime;

    @NotNull
    private LocalDateTime endDateTime;

    private String description;
}
