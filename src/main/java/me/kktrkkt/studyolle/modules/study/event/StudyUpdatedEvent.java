package me.kktrkkt.studyolle.modules.study.event;

import lombok.Getter;
import me.kktrkkt.studyolle.modules.study.Study;
import org.springframework.context.ApplicationEvent;

@Getter
public class StudyUpdatedEvent{

    private final Study study;

    private final String message;

    public StudyUpdatedEvent(Study study, String message) {
        this.study = study;
        this.message = message;
    }
}
