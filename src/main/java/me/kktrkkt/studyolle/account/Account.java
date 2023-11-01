package me.kktrkkt.studyolle.account;

import lombok.*;
import me.kktrkkt.studyolle.model.BaseEntity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@Builder @AllArgsConstructor @NoArgsConstructor
public class Account extends BaseEntity<Account> {

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String nickname;

    private String password;

    private boolean emailVerified;

    private String emailCheckToken;

    private LocalDateTime emailCheckTokenGeneratedAt;

    private LocalDateTime joinedAt;

    private String bio;

    private String url;

    private String occupation;

    private String location;

    @Lob @Basic(fetch = FetchType.EAGER)
    private String profileImage;

    private boolean studyCreatedByEmail;

    private boolean studyCreatedByWeb = true;

    private boolean studyEnrollmentResultByEmail;

    private boolean studyEnrollmentResultByWeb = true;

    private boolean studyUpdatedByEmail;

    private boolean studyUpdatedByWeb = true;

    public Account createNew() {
        registerEvent(this);
        return this;
    }

    public void completeSignUp() {
        this.emailVerified = true;
        this.joinedAt = LocalDateTime.now();
    }
}