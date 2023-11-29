package me.kktrkkt.studyolle.account.exception;

public class NicknameNotFoundException extends RuntimeException {
    public NicknameNotFoundException(String nickname) {
        super(nickname);
    }
}
