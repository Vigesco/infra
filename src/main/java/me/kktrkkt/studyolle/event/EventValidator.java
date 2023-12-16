package me.kktrkkt.studyolle.event;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.time.LocalDateTime;

public class EventValidator implements Validator {

    @Override
    public boolean supports(Class clazz) {
        return clazz.isAssignableFrom(EventForm.class);
    }

    @Override
    public void validate(Object target, Errors errors) {
        EventForm eventForm = (EventForm) target;
        LocalDateTime startDateTime = eventForm.getStartDateTime();
        LocalDateTime endDateTime = eventForm.getEndDateTime();
        LocalDateTime endEnrollmentDateTime = eventForm.getEndEnrollmentDateTime();

        if(!startDateTime.isAfter(LocalDateTime.now())){
            errors.rejectValue("startDateTime", "startDateTime",
                    "시작일은 현재보다 이후이어야 합니다");
        }

        if(!endDateTime.isAfter(startDateTime)){
            errors.rejectValue("endDateTime", "endDateTime",
                    "종료일은 시작일보다 이전이어야 합니다.");
        }

        if(endEnrollmentDateTime.isBefore(startDateTime) || endEnrollmentDateTime.isAfter(endDateTime)) {
            errors.rejectValue("endEnrollmentDateTime", "endEnrollmentDateTime",
                    "등록 신청 마감일은 모임 시작일과 마감일 사이여야 합니다.");
        }
    }
}

