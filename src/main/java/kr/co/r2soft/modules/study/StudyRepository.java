package kr.co.r2soft.modules.study;

import kr.co.r2soft.modules.account.entity.Account;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
public interface StudyRepository extends JpaRepository<Study, Long>, StudyRepositoryExtension {

    @EntityGraph(attributePaths = {"managers", "members", "topics", "zones"}, type= EntityGraph.EntityGraphType.LOAD)
    Optional<Study> findByPath(String path);

    @EntityGraph(attributePaths = "managers", type = EntityGraph.EntityGraphType.FETCH)
    Optional<Study> findWithManagerByPath(String path);

    @EntityGraph(attributePaths = {"topics", "managers"}, type = EntityGraph.EntityGraphType.FETCH)
    Optional<Study> findWithTopicByPath(String path);

    @EntityGraph(attributePaths = {"zones", "managers"}, type = EntityGraph.EntityGraphType.FETCH)
    Optional<Study> findWithZoneByPath(String path);

    @EntityGraph(attributePaths = "members", type = EntityGraph.EntityGraphType.FETCH)
    Optional<Study> findWithMemberByPath(String path);

    @EntityGraph(attributePaths = {"members", "managers"}, type = EntityGraph.EntityGraphType.FETCH)
    Optional<Study> findWithMemberAndManagerByPath(String path);

    @EntityGraph(attributePaths = {"topics", "zones"}, type = EntityGraph.EntityGraphType.FETCH)
    Optional<Study> findWithTopicAndZoneById(Long id);

    @EntityGraph(attributePaths = {"topics", "zones"}, type = EntityGraph.EntityGraphType.FETCH)
    List<Study> findTop9ByPublishedTrueOrderByPublishedAtDesc();

    List<Study> findTop5ByClosedFalseAndManagersContainsOrderByCreatedAtDesc(Account manager);

    List<Study> findTop5ByPublishedTrueAndMembersContainsOrderByPublishedAtDesc(Account member);
}
