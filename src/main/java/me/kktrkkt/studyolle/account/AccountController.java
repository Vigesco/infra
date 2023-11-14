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

    private static final String SIGN_UP_FORM = "signUpForm";
    private static final String CHECK_EMAIL_TOKEN_FORM = "checkEmailToken";
    private static final String LOGIN_FORM = "loginForm";
    private static final String CHECK_EMAIL_FORM = "checkEmailForm";

    private final AccountService accountService;

    private final AccountRepository accounts;

    @GetMapping("/sign-up")
    public String signUpForm(Model model) {
        model.addAttribute(new SignUpForm());
        return SIGN_UP_FORM;
    }

    @PostMapping("/sign-up")
    public String signUpSubmit(@Valid SignUpForm signUpForm, Errors errors) {
        if(errors.hasErrors()){
            return SIGN_UP_FORM;
        }
        else{
            Account account = accountService.processSignUp(signUpForm);
            accountService.login(account);
            return "redirect:/";
        }
    }

    @GetMapping("/check-email-token")
    public String checkEmailToken(@RequestParam String token, @RequestParam String email, Model model){
        Optional<Account> optionalAccount = accounts.findByEmail(email);
        if(optionalAccount.isEmpty()){
            model.addAttribute("error", "wrong.email");
            return CHECK_EMAIL_TOKEN_FORM;
        }

        Account account = optionalAccount.get();
        if(!account.isValidToken(token)){
            model.addAttribute("error", "wrong.token");
            return CHECK_EMAIL_TOKEN_FORM;
        }

        account.completeSignUp();
        accountService.login(account);

        int orderByJoinedAt = getOrderByJoinedAtWithOutNull(account);
        model.addAttribute("orderByJoinedAt", orderByJoinedAt);
        model.addAttribute("nickname", account.getNickname());

        return CHECK_EMAIL_TOKEN_FORM;
    }

    private int getOrderByJoinedAtWithOutNull(Account save) {
        List<Account> findAllOrderByJoinedAt = accounts.findAll(Sort.by("joinedAt")).stream()
                .filter(x -> x.getJoinedAt() != null)
                .collect(Collectors.toList());
        return findAllOrderByJoinedAt.indexOf(save) + 1;
    }

    @GetMapping("/login")
    public String loginForm() {
        return LOGIN_FORM;
    }

    @GetMapping("/check-email")
    public String checkEmailForm(Model model) {
        return CHECK_EMAIL_FORM;
    }

    @PostMapping("/check-email")
    public String sendValidationEmail(String email, Model model) {
        Account account = accounts.findByEmail(email).orElseThrow(() -> new EmailNotFoundException(email));
        if(!account.canSendValidationEmail()) {
            model.addAttribute("error", "인증 이메일은 하루에 최대 5건만 보낼 수 있습니다!");
            return CHECK_EMAIL_FORM;
        }

        accountService.sendValidationEmail(email);
        model.addAttribute("success", "success");
        model.addAttribute(account);
        return CHECK_EMAIL_FORM;
    }

}
