package me.kktrkkt.studyolle;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithAccountSecurityContextFacotry.class)
public @interface WithAccount {

    /*
    첫번째 값만 SpringSecurityContextHolder에 담기게 된다
     */
    String[] value() default {};
}