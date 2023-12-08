package me.kktrkkt.studyolle.study;

import antlr.ASTNULLType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import me.kktrkkt.studyolle.account.CurrentUser;
import me.kktrkkt.studyolle.account.entity.Account;
import me.kktrkkt.studyolle.topic.Topic;
import me.kktrkkt.studyolle.topic.TopicForm;
import me.kktrkkt.studyolle.topic.TopicRepository;
import me.kktrkkt.studyolle.topic.TopicService;
import me.kktrkkt.studyolle.zone.Zone;
import me.kktrkkt.studyolle.zone.ZoneForm;
import me.kktrkkt.studyolle.zone.ZoneRepository;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class StudySettingsController {

    static final String SETTINGS_BASE_URL = "/study/{path}/settings";
    static final String SETTINGS_INFO_URL = SETTINGS_BASE_URL + "/info";
    static final String SETTINGS_INFO_VIEW = "study/settings/info";
    static final String SETTINGS_BANNER_URL = SETTINGS_BASE_URL + "/banner";
    static final String SETTINGS_BANNER_VIEW = "study/settings/banner";
    static final String SETTINGS_TOPIC_URL = SETTINGS_BASE_URL + "/topic";
    static final String SETTINGS_TOPIC_VIEW = "study/settings/topic";
    static final String SETTINGS_ZONE_URL = SETTINGS_BASE_URL + "/zone";
    static final String SETTINGS_ZONE_VIEW = "study/settings/zone";

    private final StudyService studyService;

    private final TopicRepository topics;

    private final TopicService topicService;

    private final ZoneRepository zones;

    private final ModelMapper modelMapper;

    private final ObjectMapper objectMapper;

    @ModelAttribute
    public Study getStudyToUpdate(@PathVariable String path, @CurrentUser Account account){
        return studyService.getStudyToUpdate(account, path);
    }

    @GetMapping(SETTINGS_INFO_URL)
    public String studySettingsInfo(Study study, Model model) {
        model.addAttribute(modelMapper.map(study, StudyInfoForm.class));
        return SETTINGS_INFO_VIEW;
    }

    @PostMapping(SETTINGS_INFO_URL)
    public String updateStudyInfo(Study study, @Valid StudyInfoForm infoForm, Errors errors,
                                  RedirectAttributes ra) {
        if(errors.hasErrors()){
            return SETTINGS_INFO_VIEW;
        }
        else {
            studyService.updateInfo(study, infoForm);
            ra.addFlashAttribute("success", "success");
            return "redirect:" + SETTINGS_INFO_URL;
        }
    }

    @GetMapping(SETTINGS_BANNER_URL)
    public String studySettingsBanner() {
        return SETTINGS_BANNER_VIEW;
    }

    @PostMapping(SETTINGS_BANNER_URL)
    public String updateStudyBanner(Study study, String banner, RedirectAttributes ra) {
        studyService.updateBanner(study, banner);
        ra.addFlashAttribute("success", "success.banner");
        return "redirect:" + SETTINGS_BANNER_URL;
    }

    @PostMapping(SETTINGS_BANNER_URL + "/{use}")
    public String updateStudyBannerUse(Study study, @PathVariable boolean use, RedirectAttributes ra) {
        studyService.updateBannerUse(study, use);
        ra.addFlashAttribute("success", "success.useBanner");
        return "redirect:" + SETTINGS_BANNER_URL;
    }

    @GetMapping(SETTINGS_TOPIC_URL)
    public String studySettingsTopic(Study study, Model model) throws JsonProcessingException {
        model.addAttribute("topicList", study.getTopics().stream().map(Topic::getTitle).collect(Collectors.toList()));

        List<String> whiteList = topics.findAll().stream()
                .map(Topic::getTitle)
                .collect(Collectors.toList());
        model.addAttribute("whiteList", objectMapper.writeValueAsString(whiteList));

        return SETTINGS_TOPIC_VIEW;
    }

    @PostMapping(SETTINGS_TOPIC_URL + "/add")
    @ResponseBody
    public ResponseEntity<Void> addTopic(Study study, @RequestBody @Valid TopicForm topicForm, Errors errors) {
        if(errors.hasErrors()){
            return ResponseEntity.badRequest().build();
        }

        Topic topic = topicService.findOrCreateNew(new Topic(), topicForm);
        studyService.addTopic(study, topic);

        return ResponseEntity.ok().build();
    }

    @PostMapping(SETTINGS_TOPIC_URL + "/remove")
    @ResponseBody
    public ResponseEntity<Void> removeTopic(Study study, @RequestBody TopicForm topicForm) {
        Optional<Topic> byTitle = topics.findByTitle(topicForm.getTitle());

        if(byTitle.isEmpty()){
            return ResponseEntity.badRequest().build();
        }

        studyService.removeTopic(study, byTitle.get());
        return ResponseEntity.ok().build();
    }

    @GetMapping(SETTINGS_ZONE_URL)
    public String studySettingsZone(Study study, Model model) throws JsonProcessingException {
        List<Zone> zoneList = study.getZones();
        model.addAttribute("zoneList", zoneList);

        List<String> whiteList = zones.findAll().stream().map(Zone::toString).collect(Collectors.toList());
        model.addAttribute("whiteList", objectMapper.writeValueAsString(whiteList));

        return SETTINGS_ZONE_VIEW;
    }

    @PostMapping(SETTINGS_ZONE_URL + "/add")
    @ResponseBody
    public ResponseEntity<Void> addZone(Study study, @RequestBody ZoneForm zoneForm) {
        Optional<Zone> byCityAndProvince = zones.findByCityAndProvince(zoneForm.getCity(), zoneForm.getProvince());
        if(byCityAndProvince.isEmpty()){
            return ResponseEntity.badRequest().build();
        }

        studyService.addZone(study, byCityAndProvince.get());

        return ResponseEntity.ok().build();
    }

    @PostMapping(SETTINGS_ZONE_URL + "/remove")
    @ResponseBody
    public ResponseEntity<Void> removeZone(Study study, @RequestBody ZoneForm zoneForm) {
        Optional<Zone> byCityAndProvince = zones.findByCityAndProvince(zoneForm.getCity(), zoneForm.getProvince());

        if(byCityAndProvince.isEmpty()){
            return ResponseEntity.badRequest().build();
        }

        studyService.removeZone(study, byCityAndProvince.get());

        return ResponseEntity.ok().build();
    }
}
