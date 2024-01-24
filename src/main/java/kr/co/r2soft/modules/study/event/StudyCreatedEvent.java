package kr.co.r2soft.modules.study.event;

import lombok.Getter;
import kr.co.r2soft.modules.study.Study;

@Getter
public class StudyCreatedEvent{

    private final Study study;

    public StudyCreatedEvent(Study study) {
        this.study = study;
    }
}
