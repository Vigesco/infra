package me.kktrkkt.studyolle.account;

import lombok.RequiredArgsConstructor;
import me.kktrkkt.studyolle.account.entity.Account;
import org.springframework.context.event.EventListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountConfig {

    private final JavaMailSender javaMailSender;

    private final AccountRepository accountRepository;

    @EventListener
    public void sendValidationEmail(Account account) {
        sendEmail(account, "스터디 올래, 회원가입  인증",
                "/check-email-token?token=" + account.getEmailCheckToken()
                        + "&email=" + account.getEmail());
    }

    public void sendLoginEmail(Account account) {
        sendEmail(account, "스터디 올래, 로그인 링크",
                "/login-by-email?token=" + account.getEmailCheckToken()
                        + "&email=" + account.getEmail());
    }

    private void sendEmail(Account account, String subject, String text) {
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setSubject(subject);
        simpleMailMessage.setText(text);
        javaMailSender.send(simpleMailMessage);
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void resetNumberOfEmailsSent() {
        accountRepository.resetNumberOfEmailsSent();
    }
}
