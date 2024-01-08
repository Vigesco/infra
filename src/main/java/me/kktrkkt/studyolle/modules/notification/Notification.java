package me.kktrkkt.studyolle.modules.notification;

import lombok.*;
import me.kktrkkt.studyolle.infra.entity.BaseEntity;
import me.kktrkkt.studyolle.modules.account.entity.Account;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@Builder @AllArgsConstructor @NoArgsConstructor
public class Notification extends BaseEntity<Notification> {

    private String link;

    private String title;

    private String message;

    private boolean checked;

    @ManyToOne
    private Account to;

    private LocalDateTime createdAt;

    @Enumerated(EnumType.ORDINAL)
    private NotificationType notificationType;
}
