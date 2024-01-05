package me.kktrkkt.studyolle.modules.event;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum EventType {

    FCFS("선착순"), CONFIRMATIVE("확인");

    private String description;

    EventType(String description) {
        this.description = description;
    }

    public static EventType valueOf(int ordinal) {
        return Arrays.stream(EventType.values())
                .filter(x->x.ordinal() == ordinal)
                .findAny()
                .orElseThrow(()->new IllegalArgumentException(ordinal + " value does not exist."));
    }
}
