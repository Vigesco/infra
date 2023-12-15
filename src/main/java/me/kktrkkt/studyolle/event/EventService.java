package me.kktrkkt.studyolle.event;

import lombok.RequiredArgsConstructor;
import me.kktrkkt.studyolle.account.entity.Account;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EventService {

    private final ModelMapper modelMapper;

    private final EventRepository events;

    public Event create(Account account, EventForm eventForm) {
        Event event = modelMapper.map(eventForm, Event.class);
        event.setCreateBy(account);
        event.setCreatedDateTime(LocalDateTime.now());
        return events.save(event);
    }
}
