package me.kktrkkt.studyolle.modules.account.entity;

import lombok.*;
import me.kktrkkt.studyolle.infra.entity.BaseEntity;
import me.kktrkkt.studyolle.modules.topic.Topic;
import me.kktrkkt.studyolle.modules.zone.Zone;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    private boolean studyCreatedByWeb = true;

    private boolean studyEnrollmentResultByEmail;

    private boolean studyEnrollmentResultByWeb = true;

    private boolean studyUpdatedByEmail;

    private boolean studyUpdatedByWeb = true;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(name = "account_account_group",
            joinColumns = @JoinColumn(name = "account_id"),
            inverseJoinColumns = @JoinColumn(name = "account_group_id"))
    private List<Authority> authorities = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "account_topic",
            joinColumns = @JoinColumn(name = "account_id"),
            inverseJoinColumns = @JoinColumn(name = "topic_id"))
    private List<Topic> topics = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "account_zone",
            joinColumns = @JoinColumn(name = "account_id"),
            inverseJoinColumns = @JoinColumn(name = "zone_id"))
    private List<Zone> zones = new ArrayList<>();

    {
        addAuthority(Authority.notVerifiedUser());
    }

    public Account createNew() {
        registerEvent(this);
        return this;
    }

    public void generateEmailToken() {
        this.emailCheckToken = String.valueOf(UUID.randomUUID());
    }

    public void completeSignUp() {
        this.emailVerified = true;
        this.joinedAt = LocalDateTime.now();
        addAuthority(Authority.user());
    }

    public void addAuthority(Authority authority) {
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