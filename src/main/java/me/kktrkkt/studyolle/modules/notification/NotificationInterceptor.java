package me.kktrkkt.studyolle.modules.notification;

import lombok.RequiredArgsConstructor;
import me.kktrkkt.studyolle.modules.account.AccountUserDetails;
import me.kktrkkt.studyolle.modules.account.entity.Account;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RequiredArgsConstructor
public class NotificationInterceptor implements HandlerInterceptor {

    private final NotificationRepository notifications;

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(modelAndView != null && !isRedirectView(modelAndView) && authentication != null && authentication.getPrincipal() instanceof  AccountUserDetails){
            Account account = ((AccountUserDetails) authentication.getPrincipal()).getAccount();
            int notificationCount = notifications.countByToAndChecked(account, false);
            modelAndView.addObject("notificationCount", notificationCount);
        }
    }

    private boolean isRedirectView(ModelAndView modelAndView) {
        return modelAndView.getViewName().startsWith("redirect:") || modelAndView.getView() instanceof RedirectView;
    }
}
