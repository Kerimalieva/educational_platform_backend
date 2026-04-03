package com.onlinelearning;

import com.onlinelearning.dto.NotificationDTO;
import com.onlinelearning.entity.Notification;
import com.onlinelearning.entity.UserAccount;
import com.onlinelearning.repository.NotificationRepository;
import com.onlinelearning.repository.UserAccountRepository;
import com.onlinelearning.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock private NotificationRepository notificationRepository;
    @Mock private UserAccountRepository userAccountRepository;

    @InjectMocks
    private NotificationService notificationService;

    private UserAccount user;
    private Notification notification;

    @BeforeEach
    void setUp() {
        user = new UserAccount();
        user.setUserAccountId(1L);

        notification = new Notification();
        notification.setNotificationId(100L);
        notification.setMessage("Test message");
        notification.setIsRead(false);
        notification.setUser(user);
    }

    @Test
    void getAllNotifications_Success() {
        when(notificationRepository.findByUserUserAccountIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(notification));

        List<NotificationDTO> result = notificationService.getAllNotifications(1L);
        assertEquals(1, result.size());
        assertEquals("Test message", result.get(0).getMessage());
    }

    @Test
    void getUnreadNotifications_Success() {
        when(notificationRepository.findByUserUserAccountIdAndIsReadFalseOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(notification));

        List<NotificationDTO> result = notificationService.getUnreadNotifications(1L);
        assertEquals(1, result.size());
        assertFalse(result.get(0).getIsRead());
    }

    @Test
    void markAsRead_Success() {
        when(notificationRepository.findById(100L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        notificationService.markAsRead(100L);
        assertTrue(notification.getIsRead());
        verify(notificationRepository).save(notification);
    }

    @Test
    void markAllAsRead_Success() {
        Notification n2 = new Notification();
        n2.setIsRead(false);
        when(notificationRepository.findByUserUserAccountIdAndIsReadFalse(1L))
                .thenReturn(List.of(notification, n2));
        when(notificationRepository.saveAll(anyList())).thenReturn(List.of());

        notificationService.markAllAsRead(1L);
        assertTrue(notification.getIsRead());
        assertTrue(n2.getIsRead());
        verify(notificationRepository).saveAll(anyList());
    }

    @Test
    void createNotification_Success() {
        when(userAccountRepository.findById(1L)).thenReturn(Optional.of(user));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        Notification created = notificationService.createNotification("Hello", "system", 1L, 999L);
        assertNotNull(created);
        verify(notificationRepository).save(any(Notification.class));
    }
}