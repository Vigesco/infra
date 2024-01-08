package me.kktrkkt.studyolle.modules.study.event;

import lombok.extern.slf4j.Slf4j;
import me.kktrkkt.studyolle.modules.study.Study;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Async
@Component
public class StudyEventListener {

    @EventListener
    public void studyCreatedEvent(StudyCreatedEvent event){
        Study study = event.getStudy();
        log.info("{} is created", study.getTitle());
    }
}
