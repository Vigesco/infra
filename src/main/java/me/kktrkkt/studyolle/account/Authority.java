package me.kktrkkt.studyolle.account;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import me.kktrkkt.studyolle.model.BaseEntity;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity @Table(name = "ACCOUNT_GROUP")
@Getter @Setter
@NoArgsConstructor
public final class Authority extends BaseEntity<Authority> implements GrantedAuthority {

    private String role;

    @Override
    public String getAuthority() {
        return this.role;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else {
            return obj instanceof Authority && this.role.equals(((Authority) obj).role);
        }
    }

    public int hashCode() {
        return this.role.hashCode();
    }

    public String toString() {
        return this.role;
    }

    public static Authority notVerifiedUser() {
        Authority authority = new Authority();
        authority.setRole("ROLE_NOT_VERIFIED_USER");
        return authority;
    }

    public static Authority user() {
        Authority authority = new Authority();
        authority.setRole("ROLE_USER");
        return authority;
    }
}
