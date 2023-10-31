package me.kktrkkt.studyolle.account;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class AccountController {

    private static final String SIGN_UP_FORM = "signUpForm";
    private static final String CHECK_EMAIL_TOKEN_FORM = "checkEmailToken";

    private final AccountService accountService;

    private final AccountRepository accounts;

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
        Optional<Account> optionalAccount = accounts.findByEmail(email);
        if(optionalAccount.isEmpty()){
            model.addAttribute("error", "wrong.email");
            return CHECK_EMAIL_TOKEN_FORM;
        }

        Account account = optionalAccount.get();

        boolean isTokenCorrect = account.getEmailCheckToken().equals(token);
        if(!isTokenCorrect){
            model.addAttribute("error", "wrong.token");
            return CHECK_EMAIL_TOKEN_FORM;
        }

        account.setEmailVerified(true);
        account.setJoinedAt(LocalDateTime.now());
        Account save = accounts.save(account);

        List<Account> findAllOrderByJoinedAt = accounts.findAll(Sort.by("joinedAt")).stream()
                .filter(x -> x.getJoinedAt() != null)
                .collect(Collectors.toList());
        int orderByJoinedAt = findAllOrderByJoinedAt.indexOf(save) + 1;

        model.addAttribute("ordreByJoinedAt", orderByJoinedAt);
        model.addAttribute("nickname", save.getNickname());

        return CHECK_EMAIL_TOKEN_FORM;
    }

}
