package me.kktrkkt.studyolle.account.model;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
public class TopicUpdateForm {

    @NotEmpty
    @Size(min = 2, max = 20)
    @Pattern(regexp = "^[가-힣A-Za-z]*$", message = "Please enter only English and Korean without spaces.")
    private String title;
}
