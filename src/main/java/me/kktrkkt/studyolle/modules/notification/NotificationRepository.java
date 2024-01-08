package me.kktrkkt.studyolle.modules.notification;

import me.kktrkkt.studyolle.modules.account.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    int countByToAndChecked(Account account, boolean checked);
}
