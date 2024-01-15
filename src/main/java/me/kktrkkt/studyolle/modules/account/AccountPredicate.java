package me.kktrkkt.studyolle.modules.account;

import com.querydsl.core.types.Predicate;
import me.kktrkkt.studyolle.modules.account.entity.QAccount;
import me.kktrkkt.studyolle.modules.topic.Topic;
import me.kktrkkt.studyolle.modules.zone.Zone;

import java.util.List;

public class AccountPredicate {

    public static Predicate findByTopicsAndZones(List<Topic> topics, List<Zone> zones) {
        return QAccount.account.topics.any().in(topics).and(QAccount.account.zones.any().in(zones));
    }
}
