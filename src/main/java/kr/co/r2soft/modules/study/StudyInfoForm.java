package kr.co.r2soft.modules.study;

import lombok.Data;

import javax.validation.constraints.Size;

@Data
public class StudyInfoForm {

    @Size(max = 255, message = "255자 이내로 입력하세요")
    private String bio;

    private String explanation;
}
