package kr.co.r2soft.modules.notification;

import kr.co.r2soft.infra.MockMvcTest;
import kr.co.r2soft.modules.account.AccountRepository;
import kr.co.r2soft.modules.account.WithAccount;
import kr.co.r2soft.modules.account.entity.Account;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@MockMvcTest
@Transactional
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NotificationRepository notifications;

    @Autowired
    private NotificationFactory notificationFactory;

    @Autowired
    private AccountRepository accounts;

    @DisplayName("안읽은 알림 목록 조회")
    @Test
    @WithAccount("user1")
    void notificationUnreadListView() throws Exception {
        Account user1 = accounts.findByNickname("user1").orElseThrow(RuntimeException::new);
        notificationFactory.createNotification(user1, "noti1");
        notificationFactory.createNotification(user1, "noti2");
        notificationFactory.createNotification(user1, "noti3");
        notificationFactory.createNotification(user1, "noti4");

        List<Notification> unreadList = notifications.findAllByToAndCheckedOrderByCreatedAt(user1, false);
        assertEquals(4, unreadList.size());

        this.mockMvc.perform(get(NotificationController.NOTIFICATION_URL + "/unread"))
                .andExpect(status().isOk())
                .andExpect(view().name("notification/view"))
                .andExpect(model().attributeExists("notificationList"))
                .andExpect(model().attributeExists("unreadCount"))
                .andExpect(model().attributeExists("readCount"))
                .andExpect(model().attributeExists("studyCreatedNotificationList"))
                .andExpect(model().attributeExists("studyUpdatedNotificationList"))
                .andExpect(model().attributeExists("eventEnrollmentNotificationList"));

        List<Notification> readList = notifications.findAllByToAndCheckedOrderByCreatedAt(user1, true);
        assertEquals(4, readList.size());
    }

    @DisplayName("읽은 알림 목록 조회")
    @Test
    @WithAccount("user1")
    void notificationReadListView() throws Exception {
        this.mockMvc.perform(get(NotificationController.NOTIFICATION_URL + "/read"))
                .andExpect(status().isOk())
                .andExpect(view().name("notification/view"))
                .andExpect(model().attributeExists("notificationList"))
                .andExpect(model().attributeExists("unreadCount"))
                .andExpect(model().attributeExists("readCount"))
                .andExpect(model().attributeExists("studyCreatedNotificationList"))
                .andExpect(model().attributeExists("studyUpdatedNotificationList"))
                .andExpect(model().attributeExists("eventEnrollmentNotificationList"));
    }

    @DisplayName("읽은 알림 목록 삭제")
    @Test
    @WithAccount("user1")
    void deleteNotificationReadList() throws Exception {
        Account user1 = accounts.findByNickname("user1").orElseThrow(RuntimeException::new);
        notificationFactory.createNotification(user1, "noti1");
        notificationFactory.createNotification(user1, "noti2");
        notificationFactory.createNotification(user1, "noti3");
        notificationFactory.createNotification(user1, "noti4");

        deleteAll();

        List<Notification> unreadList = notifications.findAllByToAndCheckedOrderByCreatedAt(user1, false);
        assertEquals(4, unreadList.size());

        unreadList.forEach(n->n.setChecked(true));

        deleteAll();

        List<Notification> readList = notifications.findAllByToAndCheckedOrderByCreatedAt(user1, true);
        assertEquals(0, readList.size());
    }

    private void deleteAll() throws Exception {
        this.mockMvc.perform(post(NotificationController.NOTIFICATION_URL + "/delete-all").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(NotificationController.NOTIFICATION_URL + "/read"));
    }
}