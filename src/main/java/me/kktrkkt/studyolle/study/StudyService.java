package me.kktrkkt.studyolle.study;

import lombok.RequiredArgsConstructor;
import me.kktrkkt.studyolle.account.entity.Account;
import me.kktrkkt.studyolle.topic.Topic;
import me.kktrkkt.studyolle.zone.Zone;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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
        return studys.findByPath(path).orElseThrow(()->new IllegalArgumentException(path + "에 해당하는 스터디를 찾을 수 없습니다!"));
    }

    public Study getStudyToUpdate(Account account, String path) {
        Study study = getStudy(path);
        if(!study.isManager(account)){
            throw new AccessDeniedException(path + "에 해당하는 스터디를 수정할 권한이 없습니다!");
        }
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
}
