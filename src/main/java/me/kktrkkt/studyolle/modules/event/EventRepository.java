package me.kktrkkt.studyolle.modules.event;

import me.kktrkkt.studyolle.modules.study.Study;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
public interface EventRepository extends JpaRepository<Event, Long> {

    @EntityGraph(attributePaths = "enrollments", type = EntityGraph.EntityGraphType.FETCH)
    Optional<Event> findWithEnrollmentById(Long id);

    List<Event> findByStudyOrderByCreatedDateTime(Study study);
}