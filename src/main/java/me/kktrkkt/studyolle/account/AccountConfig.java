package me.kktrkkt.studyolle.account;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.kktrkkt.studyolle.account.entity.Account;
import me.kktrkkt.studyolle.infra.mail.EmailService;
import org.springframework.context.event.EventListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountConfig {

    private final EmailService emailService;

    private final AccountRepository accountRepository;

    @EventListener
    public void sendValidationEmail(Account account) {
        emailService.send(account.getEmail(), "스터디 올래, 회원가입  인증",
                "/check-email-token?token=" + account.getEmailCheckToken()
                        + "&email=" + account.getEmail());
    }

    public void sendLoginEmail(Account account) {
        emailService.send(account.getEmail(), "스터디 올래, 로그인 링크",
                "/login-by-email?token=" + account.getEmailCheckToken()
                        + "&email=" + account.getEmail());
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void resetNumberOfEmailsSent() {
        accountRepository.resetNumberOfEmailsSent();
    }
}
