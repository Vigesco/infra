package me.kktrkkt.studyolle.modules.account;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.kktrkkt.studyolle.modules.account.entity.Account;
import me.kktrkkt.studyolle.infra.config.AppProperties;
import me.kktrkkt.studyolle.infra.mail.EmailService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountConfig {

    private final EmailService emailService;

    private final AccountRepository accountRepository;

    private final TemplateEngine templateEngine;

    private final AppProperties appProperties;

    @EventListener
    public void sendValidationEmail(Account account) {
        String simpleLinkHtml = simpleLinkHtmlTemplate(account.getNickname(),
                "스터디 올래 서비스를 이용하려면 링크를 클릭하세요.",
                String.format("/check-email-token?token=%s&email=%s",
                        account.getEmailCheckToken(), account.getEmail()),
                "이메일 인증 링크");
        emailService.send(account.getEmail(), "스터디 올래, 회원가입  인증", simpleLinkHtml);
    }

    public void sendLoginEmail(Account account) {
        String simpleLinkHtml = simpleLinkHtmlTemplate(account.getNickname(),
                "로그인하려면 아래 링크를 클릭하세요.",
                String.format("/login-by-email?token=%s&email=%s",
                        account.getEmailCheckToken(), account.getEmail()),
                "로그인 링크");
        emailService.send(account.getEmail(), "스터디 올래, 로그인 링크", simpleLinkHtml);
    }

    private String simpleLinkHtmlTemplate(String nickname, String message, String link, String linkName) {
        Context context = new Context();
        context.setVariable("nickname", nickname);
        context.setVariable("message", message);
        context.setVariable("host", appProperties.getHost());
        context.setVariable("link", link);
        context.setVariable("linkName", linkName);
        return templateEngine.process("mail/simple-link", context);
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void resetNumberOfEmailsSent() {
        accountRepository.resetNumberOfEmailsSent();
    }
}
