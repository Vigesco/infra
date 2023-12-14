package me.kktrkkt.studyolle.study;

import lombok.RequiredArgsConstructor;
import me.kktrkkt.studyolle.account.entity.Account;
import me.kktrkkt.studyolle.topic.Topic;
import me.kktrkkt.studyolle.zone.Zone;
import org.modelmapper.ModelMapper;
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

    public Study create(Account account, StudyForm studySubmitForm) {
        Study newStudy = modelMapper.map(studySubmitForm, Study.class);
        newStudy.getManagers().add(account);
        return studys.save(newStudy);
    }

    public void updateInfo(Study study, StudyInfoForm infoForm) {
        modelMapper.map(infoForm, study);
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
    }

    public void close(Study study) {
        study.close();
    }

    public void stopRecruiting(Study study) {
        study.stopRecruiting();
    }

    public void startRecruiting(Study study) {
        study.startRecruiting();
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

    private Study ifStudy(Optional<Study> study, String path) {
        return study.orElseThrow(()->new IllegalArgumentException(path + "에 해당하는 스터디를 찾을 수 없습니다!"));
    }

    private Study getStudyToUpdate(Account account, String path, Optional<Study> byPath) {
        Study study = ifStudy(byPath, path);
        ifManager(account, study);
        return study;
    }
}
