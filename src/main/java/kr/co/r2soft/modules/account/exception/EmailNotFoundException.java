package kr.co.r2soft.modules.account.exception;

public class EmailNotFoundException extends RuntimeException {
    public EmailNotFoundException(String email) {
        super(email);
    }
}
