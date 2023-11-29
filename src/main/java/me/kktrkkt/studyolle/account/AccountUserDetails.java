package me.kktrkkt.studyolle.account;

import lombok.Getter;
import lombok.Setter;
import me.kktrkkt.studyolle.account.entity.Account;
import org.springframework.security.core.userdetails.User;

@Getter @Setter
public class AccountUserDetails extends User {

    private Account account;

    public AccountUserDetails(Account account) {
        super(account.getEmail(), account.getPassword(), account.getAuthorities());
        this.account = account;
    }
}
