package me.kktrkkt.studyolle.account;

public class EmailNotFoundException extends RuntimeException {
    public EmailNotFoundException(String email) {
        super(email);
    }
}
