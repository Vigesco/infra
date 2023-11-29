package me.kktrkkt.studyolle.account.model;

import lombok.Data;
import me.kktrkkt.studyolle.infra.validator.Unique;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
public class NicknameUpdateForm {

    @NotEmpty(message = "Nickname should not be empty")
    @Size(min = 3, max = 20, message = "Nickname length should be between 3 and 20")
    @Unique(table="Account", column="nickname", message = "Nickname is already Existed")
    private String nickname;
}
