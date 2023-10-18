package me.kktrkkt.studyolle.account;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller

public class AccountController {

    private static final String SIGN_UP_FORM = "signUpForm";

    @GetMapping("/sign-up")
    public String signUpForm() {
        return SIGN_UP_FORM;
    }

}
