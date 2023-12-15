package me.kktrkkt.studyolle.event;

import lombok.Getter;

@Getter
public enum EventType {

    FCFS("선착순"), CONFIRMATIVE("확인");

    private String description;

    EventType(String description) {
        this.description = description;
    }
}
