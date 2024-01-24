package kr.co.r2soft.modules.notification;

import lombok.RequiredArgsConstructor;
import kr.co.r2soft.modules.account.CurrentUser;
import kr.co.r2soft.modules.account.entity.Account;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class NotificationController {

    static final String NOTIFICATION_URL = "/notification";

    private final NotificationRepository notifications;

    private final NotificationService notificationService;

    @GetMapping(NOTIFICATION_URL + "/unread")
    public String notificationUnread(@CurrentUser Account account, Model model) {
        List<Notification> notificationList = notifications.findAllByToAndCheckedOrderByCreatedAt(account, false);
        notificationService.check(notificationList);
        putCategorizeNotifications(model, notificationList, notificationList.size(), notifications.countByToAndChecked(account, true));
        return "notification/view";
    }

    @GetMapping(NOTIFICATION_URL + "/read")
    public String notificationRead(@CurrentUser Account account, Model model) {
        List<Notification> notificationList = notifications.findAllByToAndCheckedOrderByCreatedAt(account, true);
        putCategorizeNotifications(model, notificationList, notifications.countByToAndChecked(account, false), notificationList.size());
        return "notification/view";
    }

    @PostMapping(NOTIFICATION_URL + "/delete-all")
    public String deleteAllNotification(@CurrentUser Account account) {
        List<Notification> notificationList = notifications.findAllByToAndCheckedOrderByCreatedAt(account, true);
        notificationService.deleteAll(notificationList);
        return "redirect:" + NOTIFICATION_URL + "/read";
    }

    private void putCategorizeNotifications(Model model, List<Notification> notificationList, int unreadCount, int readCount) {
        List<Notification> studyCreatedNotificationList = new ArrayList<>();
        List<Notification> studyUpdatedNotificationList = new ArrayList<>();
        List<Notification> eventEnrollmentNotificationList = new ArrayList<>();

        for (Notification notification: notificationList) {
            switch (notification.getNotificationType()){
                case STUDY_CREATED: studyCreatedNotificationList.add(notification); break;
                case STUDY_UPDATED: studyUpdatedNotificationList.add(notification); break;
                case EVENT_ENROLLMENT: eventEnrollmentNotificationList.add(notification); break;
            }
        }

        model.addAttribute("notificationList", notificationList);
        model.addAttribute("unreadCount", unreadCount);
        model.addAttribute("readCount", readCount);
        model.addAttribute("studyCreatedNotificationList", studyCreatedNotificationList);
        model.addAttribute("studyUpdatedNotificationList", studyUpdatedNotificationList);
        model.addAttribute("eventEnrollmentNotificationList", eventEnrollmentNotificationList);
    }

}
