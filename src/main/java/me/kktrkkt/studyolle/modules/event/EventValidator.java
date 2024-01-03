package me.kktrkkt.studyolle.modules.event;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.time.LocalDateTime;

@Component
public class EventValidator implements Validator {

    @Override
    public boolean supports(Class clazz) {
        return EventForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        EventForm eventForm = (EventForm) target;
        LocalDateTime startDateTime = eventForm.getStartDateTime();
        LocalDateTime endDateTime = eventForm.getEndDateTime();
        LocalDateTime endEnrollmentDateTime = eventForm.getEndEnrollmentDateTime();

        if(!endEnrollmentDateTime.isAfter(LocalDateTime.now())) {
            errors.rejectValue("endEnrollmentDateTime", "endEnrollmentDateTime",
                    "등록 신청 마감일은 모임 현재보다 이후이어야합니다.");
        }

        if(!startDateTime.isAfter(endEnrollmentDateTime)){
            errors.rejectValue("startDateTime", "startDateTime",
                    "시작일은 등록 신청 마감일보다 이후이어야 합니다");
        }

        if(!endDateTime.isAfter(startDateTime)){
            errors.rejectValue("endDateTime", "endDateTime",
                    "종료일은 시작일보다 이후이어야 합니다.");
        }
    }

    public void validateUpdateForm(EventForm eventForm, Event event, Errors errors) {
        if(event.acceptCount() > eventForm.getLimitOfEnrollments()){
            errors.rejectValue("limitOfEnrollments", "limitOfEnrollments", "최대 모집 인원은 현재 모집한 인원보다 많거나 같게 설정해야합니다.");
        }
    }
}

