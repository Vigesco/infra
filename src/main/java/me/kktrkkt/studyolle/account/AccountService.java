package me.kktrkkt.studyolle.account;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final PasswordEncoder encoder;

    private final AccountRepository accounts;

    public Account processSignUp(SignUpForm signUpForm) {
        String encodePassword = encoder.encode(signUpForm.getPassword());
        String emailCheckToken = String.valueOf(UUID.randomUUID());
        Account account = Account.builder()
                .nickname(signUpForm.getNickname())
                .email(signUpForm.getEmail())
                .password(encodePassword)
                .emailCheckToken(emailCheckToken)
                .build();
        account.addAuthority(Authority.notVerifiedUser());

        return accounts.save(account.createNew());
    }

    public void login(Account account) {
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(account, null));
    }
}
