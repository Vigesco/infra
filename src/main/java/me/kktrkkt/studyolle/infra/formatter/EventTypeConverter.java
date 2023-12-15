package me.kktrkkt.studyolle.infra.formatter;

import me.kktrkkt.studyolle.event.EventType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class EventTypeConverter implements Converter<String, EventType> {

    @Override
    public EventType convert(String source) {
        return EventType.valueOf(Integer.parseInt(source));
    }
}