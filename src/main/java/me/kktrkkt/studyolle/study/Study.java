package me.kktrkkt.studyolle.study;

import lombok.*;
import me.kktrkkt.studyolle.account.AccountUserDetails;
import me.kktrkkt.studyolle.account.entity.Account;
import me.kktrkkt.studyolle.infra.entity.BaseEntity;
import me.kktrkkt.studyolle.topic.Topic;
import me.kktrkkt.studyolle.zone.Zone;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@NamedEntityGraph(
        name = "Study.withAll",
        attributeNodes = {
                @NamedAttributeNode("managers"),
                @NamedAttributeNode("members"),
                @NamedAttributeNode("zones"),
                @NamedAttributeNode("topics"),
        }
)
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
    @OrderColumn
    private List<Account> managers = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "study_member",
            joinColumns = @JoinColumn(name = "study_id"),
            inverseJoinColumns = @JoinColumn(name = "account_id"))
    @OrderColumn
    private List<Account> members = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "study_topic",
            joinColumns = @JoinColumn(name = "study_id"),
            inverseJoinColumns = @JoinColumn(name = "topic_id"))
    @OrderColumn
    private List<Topic> topics = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "study_zone",
            joinColumns = @JoinColumn(name = "study_id"),
            inverseJoinColumns = @JoinColumn(name = "zone_id"))
    @OrderColumn
    private List<Zone> zones = new ArrayList<>();

    private LocalDateTime recruitingUpdateTime;

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
        return isManager(details.getAccount());
    }

    public boolean isManager(Account account) {
        return managers.contains(account);
    }

    public boolean canPublish() {
        return !this.published && !this.closed;
    }

    public void publish() {
        if(canPublish()) {
            this.published = true;
        }
        else {
            throw new RuntimeException("The study cannot be made public. The study has already been published or ended.");
        }
    }

    public boolean canClose() {
        return this.published && !this.closed;
    }

    public void close() {
        if(canClose()) {
            this.closed = true;
        }
        else {
            throw new RuntimeException("The study cannot be made close. The study is not public or has already closed.");
        }
    }
}
