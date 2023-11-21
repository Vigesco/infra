package me.kktrkkt.studyolle.account;

import lombok.RequiredArgsConstructor;
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

    public void sendValidationEmail(String email) {
        Account account = accounts.findByEmail(email).orElseThrow(() -> new EmailNotFoundException(email));
        accountConfig.sendValidationEmail(account);
        account.plusNumberOfEmailsSentToday(1);
    }

    public void updateProfile(Account account, ProfileUpdateForm profileUpdateForm) {
        modelMapper.map(profileUpdateForm, account);
        accounts.save(account);
    }
}
