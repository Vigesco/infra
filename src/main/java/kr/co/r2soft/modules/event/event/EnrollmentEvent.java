package kr.co.r2soft.modules.event.event;

import lombok.Getter;
import kr.co.r2soft.modules.event.Enrollment;

@Getter
public class EnrollmentEvent {

    private final Enrollment enrollment;

    private final String message;

    public EnrollmentEvent(Enrollment enrollment, String message) {
        this.enrollment = enrollment;
        this.message = message;
    }
}
