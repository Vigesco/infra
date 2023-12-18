package me.kktrkkt.studyolle.study;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional(readOnly = true)
public interface StudyRepository extends JpaRepository<Study, Long> {

    @EntityGraph(value = "Study.withAll", type= EntityGraph.EntityGraphType.LOAD)
    Optional<Study> findByPath(String path);

    @EntityGraph(attributePaths = "managers", type = EntityGraph.EntityGraphType.FETCH)
    Optional<Study> findWithManagerByPath(String path);

    @EntityGraph(attributePaths = {"topics", "managers"}, type = EntityGraph.EntityGraphType.FETCH)
    Optional<Study> findWithTopicByPath(String path);

    @EntityGraph(attributePaths = {"zones", "managers"}, type = EntityGraph.EntityGraphType.FETCH)
    Optional<Study> findWithZoneByPath(String path);

    @EntityGraph(attributePaths = {"members"}, type = EntityGraph.EntityGraphType.FETCH)
    Optional<Study> findWithMemberByPath(String path);

    @EntityGraph(attributePaths = {"managers", "members", "events"}, type = EntityGraph.EntityGraphType.FETCH)
    Optional<Study> findWithEventsByPath(String path);
}
