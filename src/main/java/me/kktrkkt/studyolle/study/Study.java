package me.kktrkkt.studyolle.study;

import lombok.*;
import me.kktrkkt.studyolle.account.AccountUserDetails;
import me.kktrkkt.studyolle.account.entity.Account;
import me.kktrkkt.studyolle.infra.entity.BaseEntity;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@Builder @AllArgsConstructor @NoArgsConstructor
public class Study extends BaseEntity<Study> {

    @Column(unique = true)
    private String path;

    private String title;

    private String bio;

    @Lob
    private String explanation;

    @Lob
    private String banner;

    @ManyToMany
    @JoinTable(name = "study_manager",
            joinColumns = @JoinColumn(name = "study_id"),
            inverseJoinColumns = @JoinColumn(name = "account_id"))
    private List<Account> managers = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "study_member",
            joinColumns = @JoinColumn(name = "study_id"),
            inverseJoinColumns = @JoinColumn(name = "account_id"))
    private List<Account> members = new ArrayList<>();

    private boolean recruiting;

    private boolean published;

    private boolean closed;

    private boolean useBanner;

    public boolean isJoinable(AccountUserDetails details) {
        return !closed && published && recruiting && !isMember(details) && !isManager(details);
    }

    public boolean isMember(AccountUserDetails details) {
        return members.contains(details.getAccount());
    }

    public boolean isManager(AccountUserDetails details) {
        return managers.contains(details.getAccount());
    }
}
