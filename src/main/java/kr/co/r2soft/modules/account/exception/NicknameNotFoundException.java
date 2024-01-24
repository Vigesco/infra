package kr.co.r2soft.modules.account.exception;

public class NicknameNotFoundException extends RuntimeException {
    public NicknameNotFoundException(String nickname) {
        super(nickname);
    }
}
