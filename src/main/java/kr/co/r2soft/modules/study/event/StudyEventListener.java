package kr.co.r2soft.modules.study.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import kr.co.r2soft.infra.config.AppProperties;
import kr.co.r2soft.infra.mail.EmailService;
import kr.co.r2soft.modules.account.AccountRepository;
import kr.co.r2soft.modules.account.entity.Account;
import kr.co.r2soft.modules.notification.Notification;
import kr.co.r2soft.modules.notification.NotificationRepository;
import kr.co.r2soft.modules.notification.NotificationType;
import kr.co.r2soft.modules.account.AccountPredicate;
import kr.co.r2soft.modules.study.Study;
import kr.co.r2soft.modules.study.StudyRepository;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
        Study study = studys.findWithTopicAndZoneById(event.getStudy().getId()).orElseThrow(RuntimeException::new);
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
                                .link("/study/" + study.getPath())
                                .build());
            }
        });
    }

    @EventListener
    public void handleStudyUpdatedEvent(StudyUpdatedEvent event){
        Study study = studys.findWithMemberAndManagerByPath(event.getStudy().getPath()).orElseThrow(RuntimeException::new);
        String message = event.getMessage();
        List<Account> accountList = new ArrayList<>();
        accountList.addAll(study.getManagers());
        accountList.addAll(study.getMembers());
        accountList.forEach(a->{
            if(a.isStudyUpdatedByEmail()){
                sendStudyUpdatedEmail(study, message, a);
            }
            if(a.isStudyCreatedByWeb()){
                notifications.save(
                        Notification.builder()
                                .notificationType(NotificationType.STUDY_UPDATED)
                                .to(a)
                                .title(study.getTitle())
                                .message(message)
                                .createdAt(LocalDateTime.now())
                                .link("/study/" + study.getPath())
                                .build());
            }
        });
    }

    private void sendStudyCreatedEmail(Study study, Account account) {
        sendEmail(study, "새로운 스터디가 생겼습니다.", account, " 스터디가 생겼습니다.");
    }

    private void sendStudyUpdatedEmail(Study study, String message, Account account) {
        sendEmail(study, message, account, " 스터디에 새소식이 있습니다");
    }

    private void sendEmail(Study study, String message, Account account, String title) {
        Context context = new Context();
        context.setVariable("nickname", account.getNickname());
        context.setVariable("message", message);
        context.setVariable("host", appProperties.getHost());
        context.setVariable("link", "/study/" + study.getPath());
        context.setVariable("linkName", study.getTitle());
        String msg = templateEngine.process("mail/simple-link", context);
        emailService.send(account.getEmail(), "["+appProperties.getName()+"] " + study.getTitle() + title, msg);
    }

}
