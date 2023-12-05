package me.kktrkkt.studyolle.infra.mail;

public interface EmailService {

    void send(String to, String subject, String message);

    void send(EmailMessage emailMessage);
}
