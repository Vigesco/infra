package kr.co.r2soft.modules.notification;

import lombok.RequiredArgsConstructor;
import kr.co.r2soft.modules.account.entity.Account;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class NotificationFactory {

    private final NotificationRepository notifications;

    public Notification createNotification(Account to, String title) {
        return notifications.save(Notification.builder()
                .notificationType(NotificationType.STUDY_CREATED)
                .createdAt(LocalDateTime.now())
                .link("link")
                .message("message")
                .to(to)
                .checked(false)
                .title(title)
                .build());
    }
}
