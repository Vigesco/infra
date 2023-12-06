package me.kktrkkt.studyolle.study;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
@RequiredArgsConstructor
public class StudyService {

    private final StudyRepository studys;

    private final ModelMapper modelMapper;

    public Study create(StudyForm studySubmitForm) {
        Study newStudy = modelMapper.map(studySubmitForm, Study.class);
        return studys.save(newStudy);
    }
}
