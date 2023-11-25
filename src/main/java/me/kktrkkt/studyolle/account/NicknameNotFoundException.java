package me.kktrkkt.studyolle.account;

public class NicknameNotFoundException extends RuntimeException {
    public NicknameNotFoundException(String nickname) {
        super(nickname);
    }
}
