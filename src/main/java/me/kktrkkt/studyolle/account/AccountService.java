package me.kktrkkt.studyolle.account;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final PasswordEncoder encoder;

    private final AccountRepository accounts;

    public Account findByEmail(String email) {
        return accounts.findByEmail(email)
                .orElseThrow((()->new EmailNotFoundException(email)));
    }

    public Account processSignUp(SignUpForm signUpForm) {
        String encodePassword = encoder.encode(signUpForm.getPassword());
        String emailCheckToken = String.valueOf(UUID.randomUUID());
        Account account = Account.builder()
                .nickname(signUpForm.getNickname())
                .email(signUpForm.getEmail())
                .password(encodePassword)
                .emailCheckToken(emailCheckToken)
                .build();

        return accounts.save(account.createNew());
    }

    public boolean verifyEmailToken(String email, String token) {
        Account account = findByEmail(email);
        return account.getEmailCheckToken().equals(token);
    }
}
