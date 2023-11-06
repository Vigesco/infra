package me.kktrkkt.studyolle.account;

import lombok.*;
import me.kktrkkt.studyolle.model.BaseEntity;

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

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Authority> authorities;

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

}