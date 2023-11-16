package me.kktrkkt.studyolle.account;

import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class NicknameNotFoundException extends UsernameNotFoundException {
    public NicknameNotFoundException(String nickname) {
        super(nickname);
    }
}
