package me.kktrkkt.studyolle.modules.event.event;

import lombok.RequiredArgsConstructor;
import me.kktrkkt.studyolle.infra.config.AppProperties;
import me.kktrkkt.studyolle.infra.mail.EmailService;
import me.kktrkkt.studyolle.modules.account.entity.Account;
import me.kktrkkt.studyolle.modules.event.Enrollment;
import me.kktrkkt.studyolle.modules.event.Event;
import me.kktrkkt.studyolle.modules.notification.Notification;
import me.kktrkkt.studyolle.modules.notification.NotificationRepository;
import me.kktrkkt.studyolle.modules.notification.NotificationType;
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
            emailService.send(account.getEmail(), "[스터디올래] " + event1.getTitle() + " " + message, msg);
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
