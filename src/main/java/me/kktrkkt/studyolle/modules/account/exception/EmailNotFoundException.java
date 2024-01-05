package me.kktrkkt.studyolle.modules.account.exception;

public class EmailNotFoundException extends RuntimeException {
    public EmailNotFoundException(String email) {
        super(email);
    }
}
