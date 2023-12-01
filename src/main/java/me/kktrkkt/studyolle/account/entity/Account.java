package me.kktrkkt.studyolle.account.entity;

import lombok.*;
import me.kktrkkt.studyolle.infra.entity.BaseEntity;
import me.kktrkkt.studyolle.topic.Topic;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    private int numberOfEmailsSentToday = 0;

    private int numberOfLoginEmailsSentToday = 0;

    private LocalDateTime emailCheckTokenGeneratedAt;

    private LocalDateTime joinedAt;

    private String bio;

    private String url;

    private String occupation;

    private String location;

    @Lob @Basic(fetch = FetchType.EAGER)
    private String profileImage;

    private boolean studyCreatedByEmail;

    private boolean studyCreatedByWeb;

    private boolean studyEnrollmentResultByEmail;

    private boolean studyEnrollmentResultByWeb;

    private boolean studyUpdatedByEmail;

    private boolean studyUpdatedByWeb;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Authority> authorities;

    @OneToMany
    private List<Topic> topics;

    public Account createNew() {
        registerEvent(this);
        return this;
    }

    public void completeSignUp() {
        this.emailVerified = true;
        this.joinedAt = LocalDateTime.now();
        addAuthority(Authority.user());
    }

    public void addAuthority(Authority authority) {
        if(this.authorities == null){
            this.authorities = new ArrayList<>();
        }
        this.authorities.add(authority);
    }

    public boolean isValidToken(String token) {
        return this.emailCheckToken.equals(token);
    }

    public boolean canSendValidationEmail() {
        return this.numberOfEmailsSentToday < 5;
    }

    public boolean canSendLoginEmail() {
        return this.numberOfLoginEmailsSentToday < 5;
    }

    public void plusNumberOfEmailsSentToday(int num){
        this.numberOfEmailsSentToday += num;
    }

    public void plusNumberOfLoginEmailsSentToday(int num){
        this.numberOfLoginEmailsSentToday += num;
    }
}