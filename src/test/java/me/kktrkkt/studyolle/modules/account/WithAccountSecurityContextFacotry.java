package me.kktrkkt.studyolle.modules.account;

import lombok.RequiredArgsConstructor;
import me.kktrkkt.studyolle.modules.account.model.SignUpForm;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.Arrays;

@RequiredArgsConstructor
public class WithAccountSecurityContextFacotry implements WithSecurityContextFactory<WithAccount> {

    private final AccountFactory accountFactory;

    private final UserDetailsService userDetailsService;

    @Override
    public SecurityContext createSecurityContext(WithAccount withAccount) {
        String[] nicknames = withAccount.value();

        Arrays.stream(withAccount.value()).forEach(accountFactory::createAccount);

        UserDetails principal = userDetailsService.loadUserByUsername(nicknames[0]);
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, principal.getPassword(), principal.getAuthorities());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        return context;
    }
}