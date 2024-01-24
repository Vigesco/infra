package kr.co.r2soft.modules.study;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import kr.co.r2soft.modules.account.CurrentUser;
import kr.co.r2soft.modules.account.entity.Account;
import kr.co.r2soft.modules.topic.Topic;
import kr.co.r2soft.modules.topic.TopicForm;
import kr.co.r2soft.modules.topic.TopicRepository;
import kr.co.r2soft.modules.topic.TopicService;
import kr.co.r2soft.modules.zone.Zone;
import kr.co.r2soft.modules.zone.ZoneForm;
import kr.co.r2soft.modules.zone.ZoneRepository;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
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
    static final String SETTINGS_STUDY_URL = SETTINGS_BASE_URL + "/study";
    static final String SETTINGS_STUDY_VIEW = "study/settings/study";

    private final StudyService studyService;

    private final TopicRepository topics;

    private final TopicService topicService;

    private final ZoneRepository zones;

    private final ModelMapper modelMapper;

    private final ObjectMapper objectMapper;

    @ModelAttribute
    public Study getStudyToUpdate(@PathVariable String path, @CurrentUser Account account, HttpServletRequest request){
        if(request.getMethod().equalsIgnoreCase("get")){
            return studyService.getStudyToUpdate(account, path);
        }
        return null;
    }

    @GetMapping(SETTINGS_INFO_URL)
    public String studySettingsInfo(Study study, Model model) {
        model.addAttribute(modelMapper.map(study, StudyInfoForm.class));
        return SETTINGS_INFO_VIEW;
    }

    @PostMapping(SETTINGS_INFO_URL)
    public String updateStudyInfo(@PathVariable String path, @CurrentUser Account account,
                                  @Valid StudyInfoForm infoForm, Errors errors,
                                  Model model, RedirectAttributes ra) {
        Study study = studyService.getStudyToUpdateStatus(account, path);
        if(errors.hasErrors()){
            model.addAttribute(study);
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
    public String updateStudyBanner(@PathVariable String path, @CurrentUser Account account,
                                    String banner, RedirectAttributes ra) {
        Study study = studyService.getStudyToUpdateStatus(account, path);
        studyService.updateBanner(study, banner);
        ra.addFlashAttribute("success", "success.banner");
        return "redirect:" + SETTINGS_BANNER_URL;
    }

    @PostMapping(SETTINGS_BANNER_URL + "/{use}")
    public String updateStudyBannerUse(@PathVariable String path, @CurrentUser Account account,
                                       @PathVariable boolean use, RedirectAttributes ra) {
        Study study = studyService.getStudyToUpdateStatus(account, path);
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
    public ResponseEntity<Void> addTopic(@PathVariable String path, @CurrentUser Account account,
                                         @RequestBody @Valid TopicForm topicForm, Errors errors) {
        Study study = studyService.getStudyToUpdateTopic(account, path);
        if(errors.hasErrors()){
            return ResponseEntity.badRequest().build();
        }

        Topic topic = topicService.findOrCreateNew(new Topic(), topicForm);
        studyService.addTopic(study, topic);

        return ResponseEntity.ok().build();
    }

    @PostMapping(SETTINGS_TOPIC_URL + "/remove")
    @ResponseBody
    public ResponseEntity<Void> removeTopic(@PathVariable String path, @CurrentUser Account account,
                                            @RequestBody TopicForm topicForm) {
        Study study = studyService.getStudyToUpdateTopic(account, path);
        Optional<Topic> byTitle = topics.findByTitle(topicForm.getTitle());

        if(!byTitle.isPresent()){
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
    public ResponseEntity<Void> addZone(@PathVariable String path, @CurrentUser Account account,
                                        @RequestBody ZoneForm zoneForm) {
        Study study = studyService.getStudyToUpdateZone(account, path);
        Optional<Zone> byCityAndProvince = zones.findByCityAndProvince(zoneForm.getCity(), zoneForm.getProvince());
        if(!byCityAndProvince.isPresent()){
            return ResponseEntity.badRequest().build();
        }

        studyService.addZone(study, byCityAndProvince.get());

        return ResponseEntity.ok().build();
    }

    @PostMapping(SETTINGS_ZONE_URL + "/remove")
    @ResponseBody
    public ResponseEntity<Void> removeZone(@PathVariable String path, @CurrentUser Account account,
                                           @RequestBody ZoneForm zoneForm) {
        Study study = studyService.getStudyToUpdateZone(account, path);
        Optional<Zone> byCityAndProvince = zones.findByCityAndProvince(zoneForm.getCity(), zoneForm.getProvince());

        if(!byCityAndProvince.isPresent()){
            return ResponseEntity.badRequest().build();
        }

        studyService.removeZone(study, byCityAndProvince.get());

        return ResponseEntity.ok().build();
    }

    @GetMapping(SETTINGS_STUDY_URL)
    public String studySettingsStudy(Study study, Model model) {
        model.addAttribute(modelMapper.map(study, StudyPathForm.class));
        model.addAttribute(modelMapper.map(study, StudyTitleForm.class));
        return SETTINGS_STUDY_VIEW;
    }

    @PostMapping(SETTINGS_STUDY_URL + "/publish")
    public String updateStudyPublished(@PathVariable String path, @CurrentUser Account account,
                                       RedirectAttributes ra) {
        Study study = studyService.getStudyToUpdateStatus(account, path);
        studyService.publish(study);
        ra.addFlashAttribute("success", "success.published");
        return "redirect:" + SETTINGS_STUDY_URL;
    }

    @PostMapping(SETTINGS_STUDY_URL + "/recruiting/start")
    public String startStudyRecruiting(@PathVariable String path, @CurrentUser Account account,
                                       RedirectAttributes ra) {
        Study study = studyService.getStudyToUpdateStatus(account, path);
        studyService.startRecruiting(study);
        ra.addFlashAttribute("success", "success.recruiting.start");
        return "redirect:" + SETTINGS_STUDY_URL;
    }

    @PostMapping(SETTINGS_STUDY_URL + "/recruiting/stop")
    public String stopStudyRecruiting(@PathVariable String path, @CurrentUser Account account,
                                       RedirectAttributes ra) {
        Study study = studyService.getStudyToUpdateStatus(account, path);
        studyService.stopRecruiting(study);
        ra.addFlashAttribute("success", "success.recruiting.stop");
        return "redirect:" + SETTINGS_STUDY_URL;
    }

    @PostMapping(SETTINGS_STUDY_URL + "/close")
    public String updateStudyClose(@PathVariable String path, @CurrentUser Account account,
                                   RedirectAttributes ra) {
        Study study = studyService.getStudyToUpdateStatus(account, path);
        studyService.close(study);
        ra.addFlashAttribute("success", "success.closed");
        return "redirect:" + SETTINGS_STUDY_URL;
    }

    @PostMapping(SETTINGS_STUDY_URL + "/path")
    public String updateStudyPath(@PathVariable String path, @CurrentUser Account account,
                                  @Valid StudyPathForm studyPathForm, Errors errors,
                                  RedirectAttributes ra) {
        if(errors.hasErrors()){
            ra.addFlashAttribute("error", "error.path");
            return "redirect:" + SETTINGS_STUDY_URL;
        }
        Study study = studyService.getStudyToUpdateStatus(account, path);
        studyService.updatePath(study, studyPathForm);
        ra.addFlashAttribute("success", "success.path");
        return "redirect:" + SETTINGS_STUDY_URL.replace("{path}", study.getPath());
    }

    @PostMapping(SETTINGS_STUDY_URL + "/title")
    public String updateStudyTitle(@PathVariable String path, @CurrentUser Account account,
                                   @Valid StudyTitleForm studyTitleForm, Errors errors,
                                   RedirectAttributes ra) {
        if(errors.hasErrors()){
            ra.addFlashAttribute("error", "error.title");
            return "redirect:" + SETTINGS_STUDY_URL;
        }
        Study study = studyService.getStudyToUpdateStatus(account, path);
        studyService.updateTitle(study, studyTitleForm);
        ra.addFlashAttribute("success", "success.title");
        return "redirect:" + SETTINGS_STUDY_URL;
    }

    @PostMapping(SETTINGS_STUDY_URL + "/delete")
    public String deleteStudy(@PathVariable String path, @CurrentUser Account account) {
        Study study = studyService.getStudyToUpdateStatus(account, path);
        studyService.delete(study);
        return "redirect:/";
    }
}
