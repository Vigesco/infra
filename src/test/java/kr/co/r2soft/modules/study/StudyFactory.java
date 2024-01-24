package kr.co.r2soft.modules.study;

import lombok.RequiredArgsConstructor;
import kr.co.r2soft.modules.account.AccountRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StudyFactory {

    private final AccountRepository accounts;

    private final StudyRepository studys;

    public Study createStudy(String nickname, String path) {
        String title = "new-study";
        String bio = "bio";
        String explanation = "explanation";

        Study newStudy = new Study();
        newStudy.setPath(path);
        newStudy.setTitle(title);
        newStudy.setBio(bio);
        newStudy.setExplanation(explanation);
        newStudy.getManagers().add(accounts.findByNickname(nickname).orElseThrow(RuntimeException::new));

        return studys.save(newStudy);
    }

    public Study createStudy(String nickname) {
        return createStudy(nickname, "new-study");
    }
}
