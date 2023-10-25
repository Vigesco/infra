package me.kktrkkt.studyolle.account;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class AccountController {

    private static final String SIGN_UP_FORM = "signUpForm";

    private final PasswordEncoder encoder;

    private final AccountRepository accounts;

    private final JavaMailSender javaMailSender;

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
            processSignUp(signUpForm);
            return "redirect:/";
        }
    }

    private void processSignUp(SignUpForm signUpForm) {
        String encodePassword = encoder.encode(signUpForm.getPassword());
        String emailCheckToken = String.valueOf(UUID.randomUUID());
        Account account = Account.builder()
                .nickname(signUpForm.getNickname())
                .email(signUpForm.getEmail())
                .password(encodePassword)
                .emailCheckToken(emailCheckToken)
                .build();

        Account newAccount = accounts.save(account);

        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setSubject("스터디 올래, 회원가입  인증");
        simpleMailMessage.setText("/check-email-token?token=" + newAccount.getEmailCheckToken() +
                "&email=" + newAccount.getEmail());
        javaMailSender.send(simpleMailMessage);
    }

}
