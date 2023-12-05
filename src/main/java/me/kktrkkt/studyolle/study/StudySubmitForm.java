package me.kktrkkt.studyolle.study;

import lombok.*;
import me.kktrkkt.studyolle.infra.entity.BaseEntity;
import me.kktrkkt.studyolle.infra.validator.Unique;

import javax.persistence.Entity;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
public class StudySubmitForm{

    @NotEmpty(message = "필수 입력 값입니다.")
    @Size(min = 3, max = 20, message = "최소 3자 이상 최대 20자 이내로 입력하세요")
    @Pattern(regexp = "^[ㄱ-힣a-zA-Z0-9_-]+$", message = "공백없이 문자, 숫자, 대시(-)와 언더바(_)만 사용하세요.")
    @Unique(table="Study", column="url", message = "이미 사용중인 URL입니다")
    private String url;

    @NotEmpty(message = "필수 입력 값입니다.")
    @Max(value = 50, message = "50자 이내로 입력하세요")
    private String title;

    @Max(value = 255, message = "255자 이내로 입력하세요")
    private String bio;

    private String explanation;
}
