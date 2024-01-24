package kr.co.r2soft.infra.mail;

public interface EmailService {

    void send(String to, String subject, String message);

    void send(EmailMessage emailMessage);
}
