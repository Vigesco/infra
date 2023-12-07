package me.kktrkkt.studyolle.study;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional(readOnly = true)
public interface StudyRepository extends JpaRepository<Study, Long> {

    @EntityGraph(value = "Study.withAll", type= EntityGraph.EntityGraphType.LOAD)
    Optional<Study> findByPath(String path);
}
