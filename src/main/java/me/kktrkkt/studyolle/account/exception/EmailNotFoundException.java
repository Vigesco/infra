package me.kktrkkt.studyolle.account.exception;

public class EmailNotFoundException extends RuntimeException {
    public EmailNotFoundException(String email) {
        super(email);
    }
}
