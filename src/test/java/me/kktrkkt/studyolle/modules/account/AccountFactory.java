package me.kktrkkt.studyolle.modules.account;

import lombok.RequiredArgsConstructor;
import me.kktrkkt.studyolle.modules.account.entity.Account;
import me.kktrkkt.studyolle.modules.account.model.SignUpForm;
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
