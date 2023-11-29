package me.kktrkkt.studyolle.account;

import lombok.RequiredArgsConstructor;
import me.kktrkkt.studyolle.account.entity.Account;
import me.kktrkkt.studyolle.account.entity.Authority;
import me.kktrkkt.studyolle.account.model.PasswordUpdateForm;
import me.kktrkkt.studyolle.account.model.SignUpForm;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountService {

    private final PasswordEncoder encoder;

    private final AccountRepository accounts;

    private final AccountConfig accountConfig;

    private final ModelMapper modelMapper;

    public Account processSignUp(SignUpForm signUpForm) {
        String encodePassword = encoder.encode(signUpForm.getPassword());
        String emailCheckToken = String.valueOf(UUID.randomUUID());
        Account account = Account.builder()
                .nickname(signUpForm.getNickname())
                .email(signUpForm.getEmail())
                .password(encodePassword)
                .emailCheckToken(emailCheckToken)
                .studyCreatedByWeb(true)
                .studyEnrollmentResultByWeb(true)
                .studyUpdatedByWeb(true)
                .build();
        account.addAuthority(Authority.notVerifiedUser());

        return accounts.save(account.createNew());
    }

    public void completeSignUp(Account account){
        account.completeSignUp();
        this.login(account);
    }

    public void login(Account account) {
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(new AccountUserDetails(account), null, account.getAuthorities()));
    }

    public void sendValidationEmail(Account account) {
        accountConfig.sendValidationEmail(account);
        account.plusNumberOfEmailsSentToday(1);
    }

    public void sendLoginEmail(Account account) {
        accountConfig.sendLoginEmail(account);
        account.plusNumberOfLoginEmailsSentToday(1);
    }

    public void save(Account account, Object update) {
        modelMapper.map(update, account);
        accounts.save(account);
    }

    public void updatePassword(Account account, PasswordUpdateForm passwordUpdateForm) {
        String encode = encoder.encode(passwordUpdateForm.getPassword());
        account.setPassword(encode);
        accounts.save(account);
    }

}
