package me.kktrkkt.studyolle.account;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccountUserDetailsService implements UserDetailsService {

    private final AccountRepository accounts;

    @Override
    public UserDetails loadUserByUsername(String emailOrNickname) throws UsernameNotFoundException {
        Optional<Account> byEmailOrNickname = accounts.findByEmailOrNickname(emailOrNickname, emailOrNickname);
        Account account = byEmailOrNickname
                .orElseThrow(() -> new UsernameNotFoundException(emailOrNickname));

        return new AccountUserDetails(account);
    }
}
