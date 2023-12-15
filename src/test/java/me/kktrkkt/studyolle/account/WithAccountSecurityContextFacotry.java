package me.kktrkkt.studyolle.account;

import lombok.RequiredArgsConstructor;
import me.kktrkkt.studyolle.account.model.SignUpForm;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

@RequiredArgsConstructor
public class WithAccountSecurityContextFacotry implements WithSecurityContextFactory<WithAccount> {

    private final AccountService accountService;

    private final UserDetailsService userDetailsService;

    @Override
    public SecurityContext createSecurityContext(WithAccount withAccount) {
        String[] nicknames = withAccount.value();

        for (String nickname: nicknames) {
            SignUpForm signUpForm = new SignUpForm();
            signUpForm.setNickname(nickname);
            signUpForm.setEmail(nickname + "@email.com");
            signUpForm.setPassword("12345678");
            accountService.processSignUp(signUpForm);
        }

        UserDetails principal = userDetailsService.loadUserByUsername(nicknames[0]);
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, principal.getPassword(), principal.getAuthorities());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        return context;
    }
}