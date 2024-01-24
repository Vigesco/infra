package kr.co.r2soft.modules.study;

import com.querydsl.core.QueryResults;
import com.querydsl.jpa.JPQLQuery;
import kr.co.r2soft.modules.account.entity.Account;
import kr.co.r2soft.modules.topic.QTopic;
import kr.co.r2soft.modules.zone.QZone;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.List;

public class StudyRepositoryExtensionImpl extends QuerydslRepositorySupport implements StudyRepositoryExtension {

    public StudyRepositoryExtensionImpl() {
        super(Study.class);
    }

    @Override
    public Page<Study> findByKeyword(String keyword, Pageable pageable) {
        QStudy study = QStudy.study;
        JPQLQuery<Study> query = from(study).where(study.published.isTrue()
                .and(study.title.containsIgnoreCase(keyword)
                        .or(study.topics.any().title.containsIgnoreCase(keyword))
                        .or(study.zones.any().localNameOfCity.containsIgnoreCase(keyword))))
                .leftJoin(study.topics, QTopic.topic).fetchJoin()
                .leftJoin(study.zones, QZone.zone).fetchJoin()
                .distinct();
        JPQLQuery<Study> studyJPQLQuery = getQuerydsl().applyPagination(pageable, query);
        QueryResults<Study> studyQueryResults = studyJPQLQuery.fetchResults();
        return new PageImpl<>(studyQueryResults.getResults(), pageable, studyQueryResults.getTotal());
    }

    @Override
    public List<Study> findByAccountTopicAndZone(Account account) {
        QStudy study = QStudy.study;
        JPQLQuery<Study> query = from(study).where(study.published.isTrue()
                        .and(study.members.contains(account).not())
                        .and(study.managers.contains(account).not())
                        .and(study.topics.any().in(account.getTopics())
                        .and(study.zones.any().in(account.getZones()))))
                .leftJoin(study.topics, QTopic.topic).fetchJoin()
                .leftJoin(study.zones, QZone.zone).fetchJoin()
                .distinct()
                .orderBy(study.publishedAt.desc())
                .limit(9);
        return query.fetch();
    }
}
