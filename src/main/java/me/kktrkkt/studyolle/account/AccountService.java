package me.kktrkkt.studyolle.account;

import lombok.RequiredArgsConstructor;
import me.kktrkkt.studyolle.account.entity.Account;
import me.kktrkkt.studyolle.account.model.PasswordUpdateForm;
import me.kktrkkt.studyolle.account.model.SignUpForm;
import me.kktrkkt.studyolle.topic.Topic;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountService {

    private final PasswordEncoder encoder;

    private final AccountRepository accounts;

    private final AccountConfig accountConfig;

    private final ModelMapper modelMapper;

    public Account processSignUp(SignUpForm signUpForm) {
        signUpForm.setPassword(encoder.encode(signUpForm.getPassword()));
        Account newAccount = modelMapper.map(signUpForm, Account.class);
        newAccount.generateEmailToken();

        return accounts.save(newAccount.createNew());
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

    public void addTopic(Account account, Topic topic){
        getTopics(account).add(topic);
    }

    public void removeTopic(Account account, Topic topic){
        getTopics(account).remove(topic);
    }

    public List<Topic> getTopics(Account account) {
        return persistAccount(account).getTopics();
    }

    private Account persistAccount(Account account) {
        return accounts.findById(account.getId()).orElseThrow(() -> new UsernameNotFoundException(account.getEmail()));
    }
}
