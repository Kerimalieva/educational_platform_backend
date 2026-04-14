package com.onlinelearning.service;

import com.onlinelearning.dto.response.NotificationResponse;
import com.onlinelearning.entity.Notification;
import com.onlinelearning.entity.UserAccount;
import com.onlinelearning.exception.ResourceNotFoundException;
import com.onlinelearning.repository.NotificationRepository;
import com.onlinelearning.repository.UserAccountRepository;
import com.onlinelearning.util.ConvertHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserAccountRepository userAccountRepository;

    @Transactional(readOnly = true)
    public List<NotificationResponse> getAllNotifications(Long userId) {
        log.debug("Fetching all notifications for user {}", userId);
        return notificationRepository.findByUserUserAccountIdOrderByCreatedAtDesc(userId).stream()
                .map(ConvertHelper::toNotificationResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getUnreadNotifications(Long userId) {
        log.debug("Fetching unread notifications for user {}", userId);
        return notificationRepository.findByUserUserAccountIdAndIsReadFalseOrderByCreatedAtDesc(userId).stream()
                .map(ConvertHelper::toNotificationResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long getUnreadNotificationCount(Long userId) {
        return notificationRepository.countByUserUserAccountIdAndIsReadFalse(userId);
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        log.info("Marking notification {} as read", notificationId);
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", notificationId));
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        log.info("Marking all notifications as read for user {}", userId);
        List<Notification> notifications = notificationRepository.findByUserUserAccountIdAndIsReadFalse(userId);
        notifications.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(notifications);
    }

    @Transactional
    public NotificationResponse createNotification(String message, String type, Long userId, Long referenceId) {
        log.info("Creating notification for user {}: {}", userId, message);
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        Notification notification = new Notification(message, type, user, referenceId);
        Notification saved = notificationRepository.save(notification);
        return ConvertHelper.toNotificationResponse(saved);
    }
}