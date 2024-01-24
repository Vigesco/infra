package kr.co.r2soft.modules.account;

import lombok.RequiredArgsConstructor;
import kr.co.r2soft.modules.account.entity.Account;
import kr.co.r2soft.modules.account.model.SignUpForm;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountFactory {

    private final AccountService accountService;

    public Account createAccount(String nickname){
        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setNickname(nickname);
        signUpForm.setEmail(nickname + "@email.com");
        signUpForm.setPassword("12345678");
        return accountService.processSignUp(signUpForm);
    }
}
