package me.kktrkkt.studyolle.modules.event.event;

import lombok.Getter;
import me.kktrkkt.studyolle.modules.event.Enrollment;

@Getter
public class EnrollmentEvent {

    private final Enrollment enrollment;

    private final String message;

    public EnrollmentEvent(Enrollment enrollment, String message) {
        this.enrollment = enrollment;
        this.message = message;
    }
}
