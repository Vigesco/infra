package kr.co.r2soft.modules.account;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import kr.co.r2soft.modules.account.entity.Account;
import kr.co.r2soft.modules.account.model.NicknameUpdateForm;
import kr.co.r2soft.modules.account.model.NotificationUpdateForm;
import kr.co.r2soft.modules.account.model.PasswordUpdateForm;
import kr.co.r2soft.modules.account.model.ProfileUpdateForm;
import kr.co.r2soft.modules.topic.Topic;
import kr.co.r2soft.modules.topic.TopicRepository;
import kr.co.r2soft.modules.topic.TopicService;
import kr.co.r2soft.modules.topic.TopicForm;
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

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class SettingsController {

    static final String PROFILE_UPDATE_VIEW = "settings/profileUpdateForm";
    static final String SETTINGS_PROFILE_URL = "/settings/profile";
    static final String NOTIFICATION_UPDATE_VIEW = "settings/notificationUpdateForm";
    static final String SETTINGS_NOTIFICATION_URL = "/settings/notification";
    static final String PASSWORD_UPDATE_VIEW = "settings/passwordUpdateForm";
    static final String SETTINGS_PASSWORD_URL = "/settings/password";
    static final String ACCOUNT_UPDATE_VIEW = "settings/accountUpdateForm";
    static final String SETTINGS_ACCOUNT_URL = "/settings/account";
    static final String SETTINGS_NICKNAME_URL = "/settings/nickname";
    static final String SETTINGS_TOPIC_URL = "/settings/topic";
    static final String TOPIC_UPDATE_VIEW = "settings/topicUpdateForm";
    static final String SETTINGS_ZONE_URL = "/settings/zone";
    static final String ZONE_UPDATE_VIEW = "settings/zoneUpdateForm";

    private final AccountService accountService;

    private final ModelMapper modelMapper;

    private final TopicRepository topics;

    private final TopicService topicService;

    private final ZoneRepository zones;

    private final ObjectMapper objectMapper;

    @GetMapping(SETTINGS_PROFILE_URL)
    public String profileUpdateForm(@CurrentUser Account account, Model model) {
        ProfileUpdateForm profileUpdateForm = modelMapper.map(account, ProfileUpdateForm.class);
        model.addAttribute(profileUpdateForm);
        return PROFILE_UPDATE_VIEW;
    }

    @PostMapping(SETTINGS_PROFILE_URL)
    public String updateProfile(@CurrentUser Account account, @Valid ProfileUpdateForm profileUpdateForm, Errors errors, RedirectAttributes ra) {
        if(errors.hasErrors()){
            return PROFILE_UPDATE_VIEW;
        }
        accountService.save(account, profileUpdateForm);
        ra.addFlashAttribute("success", "success");
        return "redirect:" + SETTINGS_PROFILE_URL;
    }

    @GetMapping(SETTINGS_PASSWORD_URL)
    public String passwordUpdateForm(Model model) {
        model.addAttribute(new PasswordUpdateForm());
        return PASSWORD_UPDATE_VIEW;
    }

    @PostMapping(SETTINGS_PASSWORD_URL)
    public String updatePassword(@CurrentUser Account account, @Valid PasswordUpdateForm passwordUpdateForm, Errors errors, RedirectAttributes ra) {
        if(errors.hasErrors()){
            return PASSWORD_UPDATE_VIEW;
        }
        accountService.updatePassword(account, passwordUpdateForm);
        ra.addFlashAttribute("success", "success");
        return "redirect:" + SETTINGS_PASSWORD_URL;
    }

    @GetMapping(SETTINGS_NOTIFICATION_URL)
    public String notificationUpdateForm(@CurrentUser Account account, Model model) {
        NotificationUpdateForm notificationUpdateForm = modelMapper.map(account, NotificationUpdateForm.class);
        model.addAttribute(notificationUpdateForm);
        return NOTIFICATION_UPDATE_VIEW;
    }

    @PostMapping(SETTINGS_NOTIFICATION_URL)
    public String saveNotification(@CurrentUser Account account, NotificationUpdateForm notificationUpdateForm, Errors errors, RedirectAttributes ra) {
        if(errors.hasErrors()){
            return NOTIFICATION_UPDATE_VIEW;
        }
        accountService.save(account, notificationUpdateForm);
        ra.addFlashAttribute("success", "success");
        return "redirect:" + SETTINGS_NOTIFICATION_URL;
    }

    @GetMapping(SETTINGS_TOPIC_URL)
    public String topicUpdateForm(@CurrentUser Account account, Model model) throws JsonProcessingException {
        model.addAttribute(account);

        List<Topic> topicList = accountService.getTopics(account);
        model.addAttribute("topicList", topicList.stream().map(Topic::getTitle).collect(Collectors.toList()));

        List<String> whiteList = topics.findAll().stream()
                .map(Topic::getTitle)
                .collect(Collectors.toList());
        model.addAttribute("whiteList", objectMapper.writeValueAsString(whiteList));

        return TOPIC_UPDATE_VIEW;
    }

    @PostMapping(SETTINGS_TOPIC_URL + "/add")
    @ResponseBody
    public ResponseEntity<Void> addTopic(@CurrentUser Account account, @RequestBody @Valid TopicForm topicForm, Errors errors) {
        if(errors.hasErrors()){
            return ResponseEntity.badRequest().build();
        }

        Topic topic = topicService.findOrCreateNew(new Topic(), topicForm);
        accountService.addTopic(account, topic);

        return ResponseEntity.ok().build();
    }

    @PostMapping(SETTINGS_TOPIC_URL + "/remove")
    @ResponseBody
    public ResponseEntity<Void> removeTopic(@CurrentUser Account account, @RequestBody TopicForm topicForm) {
        Optional<Topic> byTitle = topics.findByTitle(topicForm.getTitle());

        if(!byTitle.isPresent()){
            return ResponseEntity.badRequest().build();
        }

        accountService.removeTopic(account, byTitle.get());
        return ResponseEntity.ok().build();
    }

    @GetMapping(SETTINGS_ZONE_URL)
    public String zoneUpdateForm(@CurrentUser Account account, Model model) throws JsonProcessingException {
        model.addAttribute(account);

        List<Zone> zoneList = accountService.getZones(account);
        model.addAttribute("zoneList", zoneList);

        List<String> whiteList = zones.findAll().stream().map(Zone::toString).collect(Collectors.toList());
        model.addAttribute("whiteList", objectMapper.writeValueAsString(whiteList));

        return ZONE_UPDATE_VIEW;
    }

    @PostMapping(SETTINGS_ZONE_URL + "/add")
    @ResponseBody
    public ResponseEntity<Void> addZone(@CurrentUser Account account, @RequestBody ZoneForm zoneForm) {
        Optional<Zone> byCityAndProvince = zones.findByCityAndProvince(zoneForm.getCity(), zoneForm.getProvince());
        if(!byCityAndProvince.isPresent()){
            return ResponseEntity.badRequest().build();
        }

        accountService.addZone(account, byCityAndProvince.get());

        return ResponseEntity.ok().build();
    }

    @PostMapping(SETTINGS_ZONE_URL + "/remove")
    @ResponseBody
    public ResponseEntity<Void> removeZone(@CurrentUser Account account, @RequestBody ZoneForm zoneForm) {
        Optional<Zone> byCityAndProvince = zones.findByCityAndProvince(zoneForm.getCity(), zoneForm.getProvince());

        if(!byCityAndProvince.isPresent()){
            return ResponseEntity.badRequest().build();
        }

        accountService.removeZone(account, byCityAndProvince.get());
        return ResponseEntity.ok().build();
    }

    @GetMapping(SETTINGS_ACCOUNT_URL)
    public String accountUpdateForm(@CurrentUser Account account, Model model) {
        NicknameUpdateForm nicknameUpdateForm = modelMapper.map(account, NicknameUpdateForm.class);
        model.addAttribute(nicknameUpdateForm);
        return ACCOUNT_UPDATE_VIEW;
    }

    @PostMapping(SETTINGS_NICKNAME_URL)
    public String updateNickname(@CurrentUser Account account, @Valid NicknameUpdateForm nicknameUpdateForm, Errors errors, RedirectAttributes ra) {
        if(errors.hasErrors()){
            return ACCOUNT_UPDATE_VIEW;
        }
        accountService.save(account, nicknameUpdateForm);
        ra.addFlashAttribute("success", "success");
        return "redirect:" + SETTINGS_ACCOUNT_URL;
    }
}
