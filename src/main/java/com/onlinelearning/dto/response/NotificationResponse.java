package com.onlinelearning.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private Long notificationId;
    private String message;
    private Boolean isRead;
    private LocalDateTime createdAt;
    private String notificationType;
    private Long referenceId;
    private Long userId;
}