package me.kktrkkt.studyolle.account;

import lombok.RequiredArgsConstructor;
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
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setSubject("스터디 올래, 회원가입  인증");
        simpleMailMessage.setText("/check-email-token?token=" + account.getEmailCheckToken() +
                "&email=" + account.getEmail());
        javaMailSender.send(simpleMailMessage);
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void resetNumberOfEmailsSent() {
        accountRepository.resetNumberOfEmailsSent();
    }
}
