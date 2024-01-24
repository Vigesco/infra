package kr.co.r2soft.modules.main;

import lombok.extern.slf4j.Slf4j;
import kr.co.r2soft.modules.account.CurrentUser;
import kr.co.r2soft.modules.account.entity.Account;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@ControllerAdvice
public class GlobalControllerAdvice {

    @ExceptionHandler
    public String defaultException(@CurrentUser Account account, HttpServletRequest request, RuntimeException e) {
        if(account != null) {
            log.info("'{}' requested '{}'", account.getNickname(), request.getRequestURL());
        }
        else {
            log.info("'{}' requested '{}'", request.getRemoteAddr(), request.getRequestURL());
        }

        log.error("bad request", e);
        return "error";
    }
}
