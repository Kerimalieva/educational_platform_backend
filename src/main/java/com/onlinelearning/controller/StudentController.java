package com.onlinelearning.controller;

import com.onlinelearning.dto.NotificationDTO;
import com.onlinelearning.dto.response.UserProfileResponse;
import com.onlinelearning.dto.request.UserUpdateRequest;
import com.onlinelearning.entity.UserAccount;
import com.onlinelearning.exception.AccessDeniedException;
import com.onlinelearning.service.NotificationService;
import com.onlinelearning.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/student")
public class StudentController {

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

    @PutMapping("/update_profile/{studentId}")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @PathVariable Long studentId,
            @Valid @RequestBody UserUpdateRequest updateRequest) {

        UserAccount currentUser = userService.getCurrentUser();
        if (!currentUser.getUserAccountId().equals(studentId)) {
            throw new AccessDeniedException("You can only update your own profile");
        }

        UserProfileResponse response = userService.updateUserProfile(studentId, updateRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/allnotifications/{userId}")
    public ResponseEntity<List<NotificationDTO>> getAllNotifications(@PathVariable Long userId) {
        // Проверка, что пользователь запрашивает свои уведомления
        UserAccount currentUser = userService.getCurrentUser();
        if (!currentUser.getUserAccountId().equals(userId)) {
            throw new RuntimeException("You can only view your own notifications");
        }

        List<NotificationDTO> notifications = notificationService.getAllNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unreadnotifications/{userId}")
    public ResponseEntity<List<NotificationDTO>> getUnreadNotifications(@PathVariable Long userId) {
        // Проверка, что пользователь запрашивает свои уведомления
        UserAccount currentUser = userService.getCurrentUser();
        if (!currentUser.getUserAccountId().equals(userId)) {
            throw new RuntimeException("You can only view your own notifications");
        }

        List<NotificationDTO> notifications = notificationService.getUnreadNotifications(userId);
        return ResponseEntity.ok(notifications);
    }
}