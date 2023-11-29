package me.kktrkkt.studyolle.account;

import me.kktrkkt.studyolle.account.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    boolean existsByEmail(String email);

    Optional<Account> findByEmail(String email);

    Optional<Account> findByEmailOrNickname(String email, String nickname);

    @Modifying
    @Query("update Account a set a.numberOfEmailsSentToday = 0 where a.numberOfEmailsSentToday <> 0")
    void resetNumberOfEmailsSent();

    Optional<Account> findByNickname(String nickname);
}
