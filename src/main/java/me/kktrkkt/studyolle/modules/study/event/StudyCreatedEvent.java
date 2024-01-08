package me.kktrkkt.studyolle.modules.study.event;

import lombok.Getter;
import me.kktrkkt.studyolle.modules.study.Study;

@Getter
public class StudyCreatedEvent{

    private final Study study;

    public StudyCreatedEvent(Study study) {
        this.study = study;
    }
}
