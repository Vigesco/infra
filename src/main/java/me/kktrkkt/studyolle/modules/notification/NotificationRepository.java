package me.kktrkkt.studyolle.modules.notification;

import me.kktrkkt.studyolle.modules.account.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    int countByToAndChecked(Account to, boolean checked);

    List<Notification> findAllByToAndCheckedOrderByCreatedAt(Account to, boolean checked);
}
