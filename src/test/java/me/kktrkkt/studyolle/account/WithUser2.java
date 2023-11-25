package me.kktrkkt.studyolle.account;

import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@WithUserDetails(value = "user2", userDetailsServiceBeanName = "accountUserDetailsService", setupBefore = TestExecutionEvent.TEST_EXECUTION)
public @interface WithUser2 {
}
