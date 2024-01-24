package kr.co.r2soft.modules.notification;

import lombok.*;
import kr.co.r2soft.infra.entity.BaseEntity;
import kr.co.r2soft.modules.account.entity.Account;

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
