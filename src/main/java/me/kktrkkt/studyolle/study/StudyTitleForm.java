package me.kktrkkt.studyolle.study;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
public class StudyTitleForm {

    @NotEmpty(message = "필수 입력 값입니다.")
    @Size(max = 50, message = "50자 이내로 입력하세요")
    private String title;
}
