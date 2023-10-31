package me.kktrkkt.studyolle.account;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class AccountController {

    private static final String SIGN_UP_FORM = "signUpForm";

    private final AccountService accountService;

    @GetMapping("/sign-up")
    public String signUpForm(Model model) {
        model.addAttribute(new SignUpForm());
        return SIGN_UP_FORM;
    }

    @PostMapping("/sign-up")
    @ResponseStatus(HttpStatus.CREATED)
    public String signUpSubmit(@Valid SignUpForm signUpForm, Errors errors, HttpServletResponse response) {
        if(errors.hasErrors()){
            response.setStatus(200);
            return SIGN_UP_FORM;
        }
        else{
            accountService.processSignUp(signUpForm);
            return "redirect:/";
        }
    }

    @GetMapping("/check-email-token")
    public String checkEmailToken(@RequestParam String token, @RequestParam String email, Model model){
        boolean isTokenCorrect = accountService.verifyEmailToken(email, token);

        model.addAttribute("isTokenCorrect", isTokenCorrect);
        if(isTokenCorrect){
            Account account = accountService.findByEmail(email);
            model.addAttribute("message", "이메일을 확인했습니다." +
                    account.getId() + "번째 회원, " +
                    account.getNickname() + "님 가입을 축하드립니다.");
        }

        return "checkEmailToken";

    }




}
