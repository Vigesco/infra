package me.kktrkkt.studyolle.modules.account;

import me.kktrkkt.studyolle.modules.account.entity.Account;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional(readOnly = true)
public interface AccountRepository extends JpaRepository<Account, Long>, QuerydslPredicateExecutor<Account> {

    boolean existsByEmail(String email);

    Optional<Account> findByEmail(String email);

    Optional<Account> findByEmailOrNickname(String email, String nickname);

    @Modifying
    @Query("update Account a set a.numberOfEmailsSentToday = 0 where a.numberOfEmailsSentToday <> 0")
    void resetNumberOfEmailsSent();

    Optional<Account> findByNickname(String nickname);

    int countAllByJoinedAtNotNullOrderByJoinedAt();

    @EntityGraph(attributePaths = {"topics", "zones", "authorities"}, type = EntityGraph.EntityGraphType.FETCH)
    Optional<Account> findWithTopicAndZoneAndAuthorityById(Long id);
}
