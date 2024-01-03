package me.kktrkkt.studyolle.study;

import me.kktrkkt.studyolle.account.AccountRepository;
import me.kktrkkt.studyolle.infra.MockMvcTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@MockMvcTest
public class StudyBaseTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected StudyRepository studys;

    @Autowired
    protected AccountRepository accounts;

    protected Study createStudy(String nickname, String path) {
        String title = "new-study";
        String bio = "bio";
        String explanation = "explanation";

        Study newStudy = new Study();
        newStudy.setPath(path);
        newStudy.setTitle(title);
        newStudy.setBio(bio);
        newStudy.setExplanation(explanation);
        newStudy.getManagers().add(accounts.findByNickname(nickname).orElseThrow());

        return studys.save(newStudy);
    }

    protected Study createStudy(String nickname) {
        return createStudy(nickname, "new-study");
    }

    protected String replacePath(String path, String settingsZoneUrl) {
        return settingsZoneUrl.replace("{path}", path);
    }
}
