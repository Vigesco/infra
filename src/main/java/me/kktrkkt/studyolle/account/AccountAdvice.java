package me.kktrkkt.studyolle.account;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class AccountAdvice {

    @ExceptionHandler(EmailNotFoundException.class)
    public String emailNotFoundExceptionHandler(EmailNotFoundException e, Model model) {
        model.addAttribute("message", e.getMessage());
        return "404";
    }
}
