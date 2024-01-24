package kr.co.r2soft.modules.account;

import com.querydsl.core.types.Predicate;
import kr.co.r2soft.modules.account.entity.QAccount;
import kr.co.r2soft.modules.topic.Topic;
import kr.co.r2soft.modules.zone.Zone;

import java.util.List;

public class AccountPredicate {

    public static Predicate findByTopicsAndZones(List<Topic> topics, List<Zone> zones) {
        return QAccount.account.topics.any().in(topics).and(QAccount.account.zones.any().in(zones));
    }
}
