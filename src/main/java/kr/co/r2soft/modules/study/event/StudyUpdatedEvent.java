package kr.co.r2soft.modules.study.event;

import lombok.Getter;
import kr.co.r2soft.modules.study.Study;

@Getter
public class StudyUpdatedEvent{

    private final Study study;

    private final String message;

    public StudyUpdatedEvent(Study study, String message) {
        this.study = study;
        this.message = message;
    }
}
