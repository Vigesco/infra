package me.kktrkkt.studyolle.modules.study;

import com.querydsl.jpa.JPQLQuery;
import me.kktrkkt.studyolle.modules.account.entity.QAccount;
import me.kktrkkt.studyolle.modules.account.entity.QAuthority;
import me.kktrkkt.studyolle.modules.topic.QTopic;
import me.kktrkkt.studyolle.modules.zone.QZone;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.List;

public class StudyRepositoryExtensionImpl extends QuerydslRepositorySupport implements StudyRepositoryExtension {

    public StudyRepositoryExtensionImpl() {
        super(Study.class);
    }

    @Override
    public List<Study> findByKeyword(String keyword) {
        QStudy study = QStudy.study;
        JPQLQuery<Study> query = from(study).where(study.published.isTrue()
                .and(study.title.containsIgnoreCase(keyword)
                        .or(study.topics.any().title.containsIgnoreCase(keyword))
                        .or(study.zones.any().localNameOfCity.containsIgnoreCase(keyword))))
                .leftJoin(study.topics, QTopic.topic).fetchJoin()
                .leftJoin(study.zones, QZone.zone).fetchJoin()
                .leftJoin(study.members, QAccount.account).fetchJoin()
                .leftJoin(QAccount.account.authorities, QAuthority.authority1).fetchJoin()
                .distinct();
        return query.fetch();
    }
}
