package me.kktrkkt.studyolle.event;

import lombok.RequiredArgsConstructor;
import me.kktrkkt.studyolle.account.entity.Account;
import me.kktrkkt.studyolle.study.Study;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EventService {

    private final ModelMapper modelMapper;

    private final EventRepository events;

    public Event create(Account account, EventForm eventForm, Study study) {
        Event event = modelMapper.map(eventForm, Event.class);
        event.setCreatedBy(account);
        event.setCreatedDateTime(LocalDateTime.now());
        event.setStudy(study);
        return events.save(event);
    }
}
