package kr.co.r2soft.modules.account;

import lombok.RequiredArgsConstructor;
import kr.co.r2soft.modules.account.entity.Account;
import kr.co.r2soft.modules.account.exception.EmailNotFoundException;
import kr.co.r2soft.modules.account.model.SignUpForm;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.util.Optional;

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
    static final String LOGIN_WITHOUT_PASSWORD_URL = "/login-without-password";
    static final String LOGIN_WITHOUT_PASSWORD_VIEW = "loginWithoutPasswordForm";
    static final String LOGIN_BY_EMAIL_URL = "/login-by-email";
    static final String LOGIN_BY_EMAIL_VIEW = "loginByEmail";

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

        if(!optionalAccount.isPresent()){
            model.addAttribute(error, wrongEmail);
            return CHECK_EMAIL_TOKEN_VIEW;
        }

        Account account = optionalAccount.get();
        if(!account.isValidToken(token)){
            model.addAttribute(error, wrongToken);
            return CHECK_EMAIL_TOKEN_VIEW;
        }

        accountService.completeSignUp(account);

        int orderByJoinedAt = accounts.countAllByJoinedAtNotNullOrderByJoinedAt();
        model.addAttribute("orderByJoinedAt", orderByJoinedAt);
        model.addAttribute("nickname", account.getNickname());

        return CHECK_EMAIL_TOKEN_VIEW;
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
        Account account = accounts.findByEmail(email)
                .orElseThrow(() -> new EmailNotFoundException(email));
        if(!account.canSendValidationEmail()) {
            model.addAttribute("error", "인증 이메일은 하루에 최대 5건만 보낼 수 있습니다!");
            return CHECK_EMAIL_VIEW;
        }

        accountService.sendValidationEmail(account);
        model.addAttribute("success", "success");
        model.addAttribute(account);
        return CHECK_EMAIL_VIEW;
    }

    @GetMapping(LOGIN_WITHOUT_PASSWORD_URL)
    public String loginWithoutPasswordForm() {
        return LOGIN_WITHOUT_PASSWORD_VIEW;
    }

    @PostMapping(LOGIN_WITHOUT_PASSWORD_URL)
    public String loginWithoutPassword(@RequestParam String email, Model model) {
        Optional<Account> byEmail = accounts.findByEmail(email);

        if(!byEmail.isPresent()){
            model.addAttribute("error", "이메일을 찾을 수 없습니다!");
            return LOGIN_WITHOUT_PASSWORD_VIEW;
        }

        Account account = byEmail.get();

        if(!account.canSendLoginEmail()) {
            model.addAttribute("error", "로그인 이메일은 하루에 최대 5건만 보낼 수 있습니다!");
            return LOGIN_WITHOUT_PASSWORD_VIEW;
        }

        accountService.sendLoginEmail(account);
        model.addAttribute("success", "");
        return LOGIN_WITHOUT_PASSWORD_VIEW;
    }

    @GetMapping(LOGIN_BY_EMAIL_URL)
    public String loginByEmailView(@RequestParam String email, @RequestParam String token, Model model) {
        Optional<Account> optionalAccount = accounts.findByEmail(email);

        if(!optionalAccount.isPresent()){
            model.addAttribute("error", "wrong.email");
            return LOGIN_BY_EMAIL_VIEW;
        }

        Account account = optionalAccount.get();
        if(!account.isValidToken(token)){
            model.addAttribute("error", "wrong.token");
            return LOGIN_BY_EMAIL_VIEW;
        }

        accountService.login(account);
        return LOGIN_BY_EMAIL_VIEW;
    }
}
