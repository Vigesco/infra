package me.kktrkkt.studyolle.modules.study;

import com.querydsl.core.QueryResults;
import com.querydsl.jpa.JPQLQuery;
import me.kktrkkt.studyolle.modules.topic.QTopic;
import me.kktrkkt.studyolle.modules.zone.QZone;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

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
}
