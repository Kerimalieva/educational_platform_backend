package com.onlinelearning.controller;

import com.onlinelearning.dto.NotificationDTO;
import com.onlinelearning.dto.UserProfileResponse;
import com.onlinelearning.dto.UserUpdateRequest;
import com.onlinelearning.entity.UserAccount;
import com.onlinelearning.service.NotificationService;
import com.onlinelearning.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/instructor")
public class InstructorController {

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

    @PutMapping("/update_profile/{instructorId}")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @PathVariable Long instructorId,
            @Valid @RequestBody UserUpdateRequest updateRequest) {

        // Проверка, что текущий пользователь обновляет свой профиль
        UserAccount currentUser = userService.getCurrentUser();
        if (!currentUser.getUserAccountId().equals(instructorId)) {
            throw new RuntimeException("You can only update your own profile");
        }

        UserAccount updatedUser = userService.updateUserProfile(instructorId, updateRequest);
        UserProfileResponse response = userService.convertToProfileResponse(updatedUser);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/notifications/{userId}")
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