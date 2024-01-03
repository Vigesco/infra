package me.kktrkkt.studyolle.modules.event;

import lombok.*;
import me.kktrkkt.studyolle.modules.account.entity.Account;
import me.kktrkkt.studyolle.infra.entity.BaseEntity;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
public class Enrollment extends BaseEntity<Enrollment> {

    @ManyToOne
    private Event event;

    @ManyToOne
    private Account account;

    private LocalDateTime enrolledAt;

    private boolean accepted;

    private boolean attended;

    public boolean canAccept() {
        return !this.accepted;
    }

    public boolean canReject() {
        return this.accepted && !this.attended;
    }

    public boolean canAttend() {
        return this.accepted && !this.attended;
    }

    public boolean canCancelAttend() {
        return this.attended;
    }

    public void accept() {
        if(canAccept()){
            this.accepted = true;
        }
        else {
            throw new RuntimeException("You cannot accept. It's already accepted");
        }
    }

    public void reject() {
        if(canReject()){
            this.accepted = false;
        }
        else {
            throw new RuntimeException("You cannot reject. It has already been rejected or attended.");
        }
    }

    public void attend() {
        if(canAttend()){
            this.attended = true;
        }
        else {
            throw new RuntimeException("Unable to attend. Already attended or not accepted");
        }
    }

    public void cancelAttend() {
        if(canCancelAttend()){
            this.attended = false;
        }
        else {
            throw new RuntimeException("Attendance cannot be cancelled. Attendance has already been canceled");
        }
    }
}
