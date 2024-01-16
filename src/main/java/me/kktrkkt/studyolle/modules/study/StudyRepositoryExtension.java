package me.kktrkkt.studyolle.modules.study;

import me.kktrkkt.studyolle.modules.account.entity.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface StudyRepositoryExtension {

    Page<Study> findByKeyword(String keyword, Pageable pageable);

    List<Study> findByAccountTopicAndZone(Account accountWithTopicAndZone);
}
