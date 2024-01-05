package me.kktrkkt.studyolle.modules.account.model;

import lombok.Getter;
import lombok.Setter;
import me.kktrkkt.studyolle.infra.validator.Unique;

import javax.validation.constraints.*;

@Getter @Setter
public class SignUpForm {

    @NotEmpty(message = "Nickname should not be empty")
    @Size(min = 3, max = 20, message = "Nickname length should be between 3 and 20")
    @Unique(table="Account", column="nickname", message = "Nickname is already Existed")
    private String nickname;

    @NotEmpty(message = "Email should not be empty")
    @Email(message = "Please provide a valid email address")
    @Unique(table="Account", column="email", message = "Email is already Existed")
    private String email;

    @NotEmpty(message = "Password should not be empty")
    @Size(min = 8, max = 50, message = "Password length should be between 8 and 50")
    @Pattern(regexp = "^[a-zA-Z0-9!@#$%^&*]+$", message = "Password can only contain English letters, numbers, and special characters")
    private String password;
}
