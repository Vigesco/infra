package me.kktrkkt.studyolle.modules.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationService {

    public void check(List<Notification> notificationList) {
        notificationList.forEach(x->x.setChecked(true));
    }
}
