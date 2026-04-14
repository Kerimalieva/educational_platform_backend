package com.onlinelearning;

import com.onlinelearning.dto.response.NotificationResponse;
import com.onlinelearning.entity.Notification;
import com.onlinelearning.entity.UserAccount;
import com.onlinelearning.exception.ResourceNotFoundException;
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
        user.setEmail("user@test.com");

        notification = new Notification();
        notification.setNotificationId(100L);
        notification.setMessage("Test message");
        notification.setIsRead(false);
        notification.setUser(user);
        notification.setNotificationType("system");
        notification.setReferenceId(999L);
    }

    @Test
    void getAllNotifications_Success() {
        when(notificationRepository.findByUserUserAccountIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(notification));

        List<NotificationResponse> result = notificationService.getAllNotifications(1L);

        assertEquals(1, result.size());
        assertEquals("Test message", result.get(0).getMessage());
        assertFalse(result.get(0).getIsRead());
        assertEquals(1L, result.get(0).getUserId());
        verify(notificationRepository).findByUserUserAccountIdOrderByCreatedAtDesc(1L);
    }

    @Test
    void getUnreadNotifications_Success() {
        when(notificationRepository.findByUserUserAccountIdAndIsReadFalseOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(notification));

        List<NotificationResponse> result = notificationService.getUnreadNotifications(1L);

        assertEquals(1, result.size());
        assertFalse(result.get(0).getIsRead());
    }

    @Test
    void getUnreadNotificationCount_Success() {
        when(notificationRepository.countByUserUserAccountIdAndIsReadFalse(1L)).thenReturn(5L);

        long count = notificationService.getUnreadNotificationCount(1L);

        assertEquals(5L, count);
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
    void markAsRead_NotificationNotFound_ThrowsResourceNotFoundException() {
        when(notificationRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> notificationService.markAsRead(999L));
        assertEquals("Notification not found with id: 999", ex.getMessage());
    }

    @Test
    void markAllAsRead_Success() {
        Notification n2 = new Notification();
        n2.setIsRead(false);
        n2.setUser(user);
        List<Notification> notifications = List.of(notification, n2);

        when(notificationRepository.findByUserUserAccountIdAndIsReadFalse(1L)).thenReturn(notifications);
        when(notificationRepository.saveAll(anyList())).thenReturn(notifications);

        notificationService.markAllAsRead(1L);

        assertTrue(notification.getIsRead());
        assertTrue(n2.getIsRead());
        verify(notificationRepository).saveAll(notifications);
    }

    @Test
    void markAllAsRead_NoUnreadNotifications_Success() {
        when(notificationRepository.findByUserUserAccountIdAndIsReadFalse(1L)).thenReturn(List.of());
        when(notificationRepository.saveAll(anyList())).thenReturn(List.of());

        notificationService.markAllAsRead(1L);

        verify(notificationRepository).saveAll(anyList());
    }

    @Test
    void createNotification_Success() {
        when(userAccountRepository.findById(1L)).thenReturn(Optional.of(user));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        NotificationResponse response = notificationService.createNotification("Hello", "system", 1L, 999L);

        assertNotNull(response);
        assertEquals("Test message", response.getMessage());
        assertEquals("system", response.getNotificationType());
        assertEquals(999L, response.getReferenceId());
        assertEquals(1L, response.getUserId());
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void createNotification_UserNotFound_ThrowsResourceNotFoundException() {
        when(userAccountRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> notificationService.createNotification("Hi", "system", 99L, 123L));
        assertEquals("User not found with id: 99", ex.getMessage());
    }
}