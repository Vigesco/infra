package kr.co.r2soft.modules.event;

import kr.co.r2soft.modules.account.entity.Account;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    @EntityGraph(attributePaths = {"event", "event.study"}, type = EntityGraph.EntityGraphType.FETCH)
    List<Enrollment> findAllByAccountAndAcceptedTrueAndAttendedFalseOrderByEnrolledAtDesc(Account account);
}
