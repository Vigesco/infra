package me.kktrkkt.studyolle.infra.mail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Slf4j
@Profile({"local", "test"})
@RequiredArgsConstructor
@Service
public class ConsoleEmailService implements EmailService {

    @Override
    public void send(String to, String subject, String message) {
        send(new EmailMessage(to, subject, message));
    }

    @Override
    public void send(EmailMessage emailMessage) {
        log.info("send to email: {}", emailMessage.getMessage());
    }
}
