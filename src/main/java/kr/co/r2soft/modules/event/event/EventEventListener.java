package kr.co.r2soft.modules.event.event;

import lombok.RequiredArgsConstructor;
import kr.co.r2soft.infra.config.AppProperties;
import kr.co.r2soft.infra.mail.EmailService;
import kr.co.r2soft.modules.account.entity.Account;
import kr.co.r2soft.modules.event.Enrollment;
import kr.co.r2soft.modules.event.Event;
import kr.co.r2soft.modules.notification.Notification;
import kr.co.r2soft.modules.notification.NotificationRepository;
import kr.co.r2soft.modules.notification.NotificationType;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class EventEventListener {

    private final NotificationRepository notifications;

    private final EmailService emailService;

    private final TemplateEngine templateEngine;

    private final AppProperties appProperties;

    @EventListener
    public void handleEnrollmentEvent(EnrollmentEvent event) {
        Enrollment enrollment = event.getEnrollment();
        String message = event.getMessage();
        Account account = enrollment.getAccount();
        Event event1 = enrollment.getEvent();
        if(account.isStudyEnrollmentResultByEmail()){
            Context context = new Context();
            context.setVariable("nickname", account.getNickname());
            context.setVariable("message", message);
            context.setVariable("host", appProperties.getHost());
            context.setVariable("link", "/study/" + event1.getStudy().getPath() + "/event/"+ event1.getId());
            context.setVariable("linkName", event1.getTitle());
            String msg = templateEngine.process("mail/simple-link", context);
            emailService.send(account.getEmail(), "["+appProperties.getName()+"] " + event1.getTitle() + " " + message, msg);
        }
        if(account.isStudyEnrollmentResultByWeb()){
            notifications.save(
                    Notification.builder()
                            .notificationType(NotificationType.EVENT_ENROLLMENT)
                            .to(account)
                            .title(event1.getTitle())
                            .message(message)
                            .createdAt(LocalDateTime.now())
                            .link("/study/" + event1.getStudy().getPath() + "/event/"+ event1.getId())
                            .build());
        }
    }

}
