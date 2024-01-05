package me.kktrkkt.studyolle.modules.account.exception;

public class NicknameNotFoundException extends RuntimeException {
    public NicknameNotFoundException(String nickname) {
        super(nickname);
    }
}
