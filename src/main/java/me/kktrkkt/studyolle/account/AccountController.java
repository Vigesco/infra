package me.kktrkkt.studyolle.account;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller

public class AccountController {

    private static final String SIGN_UP_FORM = "signUpForm";

    @GetMapping("/sign-up")
    public String signUpForm(Model model) {
        model.addAttribute("signUpForm", new SignUpForm());
        return SIGN_UP_FORM;
    }

}
