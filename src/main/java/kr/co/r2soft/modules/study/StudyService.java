package kr.co.r2soft.modules.study;

import lombok.RequiredArgsConstructor;
import kr.co.r2soft.modules.account.entity.Account;
import kr.co.r2soft.modules.study.event.StudyCreatedEvent;
import kr.co.r2soft.modules.study.event.StudyUpdatedEvent;
import kr.co.r2soft.modules.topic.Topic;
import kr.co.r2soft.modules.zone.Zone;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional
@Service
@RequiredArgsConstructor
public class StudyService {

    private final StudyRepository studys;

    private final ModelMapper modelMapper;

    private final ApplicationEventPublisher eventPublisher;

    public Study create(Account account, StudyForm studySubmitForm) {
        Study newStudy = modelMapper.map(studySubmitForm, Study.class);
        newStudy.getManagers().add(account);
        Study save = studys.save(newStudy);
        return save;
    }

    public void updateInfo(Study study, StudyInfoForm infoForm) {
        modelMapper.map(infoForm, study);
        eventPublisher.publishEvent(new StudyUpdatedEvent(study, "스터디 소개를 수정했습니다."));
    }

    public Study getStudy(String path) {
        Optional<Study> byPath = studys.findByPath(path);
        return ifStudy(byPath, path);
    }

    public Study getStudyToUpdate(Account account, String path) {
        Optional<Study> byPath = studys.findByPath(path);
        return getStudyToUpdate(account, path, byPath);
    }

    public Study getStudyToUpdateStatus(Account account, String path) {
        Optional<Study> byPath = studys.findWithManagerByPath(path);
        return getStudyToUpdate(account, path, byPath);
    }

    public Study getStudyToUpdateTopic(Account account, String path) {
        Optional<Study> byPath = studys.findWithTopicByPath(path);
        return getStudyToUpdate(account, path, byPath);
    }

    public Study getStudyToUpdateZone(Account account, String path) {
        Optional<Study> byPath = studys.findWithZoneByPath(path);
        return getStudyToUpdate(account, path, byPath);
    }

    public Study getStudyToMember(String path) {
        Optional<Study> byPath = studys.findWithMemberByPath(path);
        return ifStudy(byPath, path);
    }

    public Study getStudyToMemberAndManager(String path, Account account) {
        Optional<Study> byPath = studys.findWithMemberAndManagerByPath(path);
        Study study = ifStudy(byPath, path);
        ifMember(study, account);
        return study;
    }

    public Study getStudyToEvent(String path, Account account) {
        Optional<Study> byPath = studys.findByPath(path);
        Study study = ifStudy(byPath, path);
        ifMember(study, account);
        return study;
    }

    public void updateBanner(Study study, String banner) {
        study.setBanner(banner);
    }

    public void updateBannerUse(Study study, boolean use) {
        study.setUseBanner(use);
    }

    public void addTopic(Study study, Topic topic) {
        study.getTopics().add(topic);
    }

    public void removeTopic(Study study, Topic topic) {
        study.getTopics().remove(topic);
    }

    public void addZone(Study study, Zone zone) {
        study.getZones().add(zone);
    }

    public void removeZone(Study study, Zone zone) {
        study.getZones().remove(zone);
    }

    public void publish(Study study) {
        study.publish();
        eventPublisher.publishEvent(new StudyCreatedEvent(study));
    }

    public void close(Study study) {
        study.close();
        eventPublisher.publishEvent(new StudyUpdatedEvent(study, "스터디 종료했습니다."));
    }

    public void stopRecruiting(Study study) {
        study.stopRecruiting();
        eventPublisher.publishEvent(new StudyUpdatedEvent(study, "팀원 모집을 중지합니다."));
    }

    public void startRecruiting(Study study) {
        study.startRecruiting();
        eventPublisher.publishEvent(new StudyUpdatedEvent(study, "팀원 모집을 시작합니다."));
    }

    public void updatePath(Study study, StudyPathForm studyPathForm) {
        modelMapper.map(studyPathForm, study);
    }

    public void updateTitle(Study study, StudyTitleForm studyTitleForm) {
        modelMapper.map(studyTitleForm, study);
    }

    private void ifManager(Account account, Study study) {
        if(!study.isManager(account)){
            throw new AccessDeniedException("해당 기능을 수정할 권한이 없습니다!");
        }
    }

    private void ifMember(Study study, Account account) {
        if(!study.isManager(account) && !study.isMember(account)){
            throw new AccessDeniedException("해당 기능을 접근할 권한이 없습니다!");
        }
    }

    private Study ifStudy(Optional<Study> study, String path) {
        return study.orElseThrow(()->new IllegalArgumentException(path + "에 해당하는 스터디를 찾을 수 없습니다!"));
    }

    private Study getStudyToUpdate(Account account, String path, Optional<Study> byPath) {
        Study study = ifStudy(byPath, path);
        ifManager(account, study);
        return study;
    }

    public void delete(Study study) {
        if(study.isRemovable()){
            studys.delete(study);
        }
        else {
            throw new RuntimeException("Study deletion is not possible. Studies that are public or have created a meeting cannot be deleted.");
        }
    }

    public void addMember(Study study, Account account) {
        study.addMember(account);
    }

    public void removeMember(Study study, Account account) {
        study.removeMember(account);
    }
}
