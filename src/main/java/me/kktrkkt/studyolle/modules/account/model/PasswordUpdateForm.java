package me.kktrkkt.studyolle.modules.account.model;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
public class PasswordUpdateForm {

    @NotEmpty(message = "Password should not be empty")
    @Size(min = 8, max = 50, message = "Password length should be between 8 and 50")
    @Pattern(regexp = "^[a-zA-Z0-9!@#$%^&*]+$", message = "Password can only contain English letters, numbers, and special characters")
    private String password;
}
