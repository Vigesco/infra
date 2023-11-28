package me.kktrkkt.studyolle.account;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class AccountController {

    static final String SIGN_UP_VIEW = "signUpForm";
    static final String CHECK_EMAIL_TOKEN_VIEW = "checkEmailToken";
    static final String LOGIN_VIEW = "loginForm";
    static final String CHECK_EMAIL_VIEW = "checkEmailForm";
    static final String SIGN_UP_URL = "/sign-up";
    static final String CHECK_EMAIL_URL = "/check-email";
    static final String CHECK_EMAIL_TOKEN_URL = "/check-email-token";
    static final String LOGIN_URL = "/login";

    private final AccountService accountService;

    private final AccountRepository accounts;

    @GetMapping(SIGN_UP_URL)
    public String signUpForm(Model model) {
        model.addAttribute(new SignUpForm());
        return SIGN_UP_VIEW;
    }

    @PostMapping(SIGN_UP_URL)
    public String signUpSubmit(@Valid SignUpForm signUpForm, Errors errors) {
        if(errors.hasErrors()){
            return SIGN_UP_VIEW;
        }
        else{
            Account account = accountService.processSignUp(signUpForm);
            accountService.login(account);
            return "redirect:/";
        }
    }

    @GetMapping(CHECK_EMAIL_TOKEN_URL)
    public String checkEmailToken(@RequestParam String token, @RequestParam String email, Model model){
        Optional<Account> optionalAccount = accounts.findByEmail(email);
        String error = "error";
        String wrongEmail = "wrong.email";
        String wrongToken = "wrong.token";

        if(optionalAccount.isEmpty()){
            model.addAttribute(error, wrongEmail);
            return CHECK_EMAIL_TOKEN_VIEW;
        }

        Account account = optionalAccount.get();
        if(!account.isValidToken(token)){
            model.addAttribute(error, wrongToken);
            return CHECK_EMAIL_TOKEN_VIEW;
        }

        accountService.completeSignUp(account);

        int orderByJoinedAt = getOrderByJoinedAtWithOutNull(account);
        model.addAttribute("orderByJoinedAt", orderByJoinedAt);
        model.addAttribute("nickname", account.getNickname());

        return CHECK_EMAIL_TOKEN_VIEW;
    }

    private int getOrderByJoinedAtWithOutNull(Account save) {
        List<Account> findAllOrderByJoinedAt = accounts.findAll(Sort.by("joinedAt")).stream()
                .filter(x -> x.getJoinedAt() != null)
                .collect(Collectors.toList());
        return findAllOrderByJoinedAt.indexOf(save) + 1;
    }

    @GetMapping(LOGIN_URL)
    public String loginForm() {
        return LOGIN_VIEW;
    }

    @GetMapping(CHECK_EMAIL_URL)
    public String checkEmailForm() {
        return CHECK_EMAIL_VIEW;
    }

    @PostMapping(CHECK_EMAIL_URL)
    public String sendValidationEmail(String email, Model model) {
        Account account = accounts.findByEmail(email).orElseThrow(() -> new EmailNotFoundException(email));
        if(!account.canSendValidationEmail()) {
            model.addAttribute("error", "인증 이메일은 하루에 최대 5건만 보낼 수 있습니다!");
            return CHECK_EMAIL_VIEW;
        }

        accountService.sendValidationEmail(email);
        model.addAttribute("success", "success");
        model.addAttribute(account);
        return CHECK_EMAIL_VIEW;
    }

}
