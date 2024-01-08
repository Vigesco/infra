package me.kktrkkt.studyolle.modules.study.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.kktrkkt.studyolle.infra.config.AppProperties;
import me.kktrkkt.studyolle.infra.mail.EmailService;
import me.kktrkkt.studyolle.modules.account.AccountRepository;
import me.kktrkkt.studyolle.modules.account.entity.Account;
import me.kktrkkt.studyolle.modules.notification.Notification;
import me.kktrkkt.studyolle.modules.notification.NotificationRepository;
import me.kktrkkt.studyolle.modules.notification.NotificationType;
import me.kktrkkt.studyolle.modules.study.AccountPredicate;
import me.kktrkkt.studyolle.modules.study.Study;
import me.kktrkkt.studyolle.modules.study.StudyRepository;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;

@Slf4j
@Async
@Component
@RequiredArgsConstructor
@Transactional
public class StudyEventListener {

    private final StudyRepository studys;

    private final AccountRepository accounts;

    private final NotificationRepository notifications;

    private final EmailService emailService;

    private final TemplateEngine templateEngine;

    private final AppProperties appProperties;

    @EventListener
    public void handleStudyCreatedEvent(StudyCreatedEvent event){
        Study study = studys.findWithTopicAndZoneById(event.getStudy().getId()).orElseThrow();
        Iterable<Account> accountList = accounts.findAll(AccountPredicate.findByTopicsAndZones(study.getTopics(), study.getZones()));
        accountList.forEach(a->{
            if(a.isStudyCreatedByEmail()){
                sendStudyCreatedEmail(study, a);
            }
            if(a.isStudyCreatedByWeb()){
                notifications.save(
                        Notification.builder()
                                .notificationType(NotificationType.STUDY_CREATED)
                                .to(a)
                                .title(study.getTitle())
                                .message(study.getBio())
                                .createdAt(LocalDateTime.now())
                                .build());
            }
        });
    }

    private void sendStudyCreatedEmail(Study study, Account account) {
        Context context = new Context();
        context.setVariable("nickname", account.getNickname());
        context.setVariable("message", "새로운 스터디가 생겼습니다.");
        context.setVariable("host", appProperties.getHost());
        context.setVariable("link", "/study/" + study.getPath());
        context.setVariable("linkName", study.getTitle());
        String message = templateEngine.process("mail/simple-link", context);
        emailService.send(account.getEmail(), study.getTitle() + " 스터디가 생겼습니다.", message);
    }
}
