package me.kktrkkt.studyolle.study;

import lombok.*;
import me.kktrkkt.studyolle.account.AccountUserDetails;
import me.kktrkkt.studyolle.account.entity.Account;
import me.kktrkkt.studyolle.infra.entity.BaseEntity;
import me.kktrkkt.studyolle.topic.Topic;
import me.kktrkkt.studyolle.zone.Zone;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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
        return isJoinable(details.getAccount());
    }

    public boolean isJoinable(Account account) {
        return !closed && published && recruiting && !isMember(account) && !isManager(account);
    }

    public boolean isMember(AccountUserDetails details) {
        return isMember(details.getAccount());
    }

    public boolean isMember(Account account) {
        return members.contains(account);
    }

    public boolean isManager(AccountUserDetails details) {
        return isManager(details.getAccount());
    }

    public boolean isManager(Account account) {
        return managers.contains(account);
    }


    public void publish() {
        if(!this.published && !this.closed) {
            this.published = true;
        }
        else {
            throw new RuntimeException("The study cannot be made public. The study has already been published or ended.");
        }
    }

    public void close() {
        if(this.published && !this.closed) {
            this.closed = true;
            this.published = false;
            this.recruiting = false;
        }
        else {
            throw new RuntimeException("The study cannot be made close. The study is not public or has already closed.");
        }
    }

    public boolean canUpdateRecruiting() {
        return this.published && (this.recruitingUpdateTime == null ||
                LocalDateTime.now().isAfter(this.recruitingUpdateTime.plus(1, ChronoUnit.HOURS)));
    }

    public void startRecruiting() {
        if(canUpdateRecruiting()){
            this.recruitingUpdateTime = LocalDateTime.now();
            this.recruiting = true;
        }
        else {
            throw new RuntimeException("Recruitment cannot start. Please make the study public or try again in 1 hour.");
        }
    }

    public void stopRecruiting() {
        if(canUpdateRecruiting()){
            this.recruitingUpdateTime = LocalDateTime.now();
            this.recruiting = false;
        }
        else {
            throw new RuntimeException("Recruitment cannot be stop. Please make the study public or try again in 1 hour.");
        }
    }

    public long recruitingUpdateRemainingMinuate(){
        return ChronoUnit.MINUTES.between(LocalDateTime.now(), this.recruitingUpdateTime.plus(1, ChronoUnit.HOURS));
    }

    public boolean isRemovable() {
        return !this.published;
    }

    public void addMember(Account account) {
        if(isJoinable(account)){
            this.members.add(account);
        }
        else {
            throw new RuntimeException("Study members cannot be added. You are already enrolled in the study, it has ended, or is not recruiting.");
        }
    }

    private boolean isWithdraw(Account account) {
        return !this.closed && isMember(account);
    }

    public void removeMember(Account account) {
        if(isWithdraw(account)) {
            this.members.remove(account);
        }
        else {
            throw new RuntimeException("Study members cannot be removed. You are not a study member or the study has ended.");
        }
    }
}
