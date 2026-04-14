package com.onlinelearning.controller;

import com.onlinelearning.dto.NotificationDTO;
import com.onlinelearning.dto.response.NotificationResponse;
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

        UserAccount currentUser = userService.getCurrentUser();
        if (!currentUser.getUserAccountId().equals(instructorId)) {
            throw new AccessDeniedException("You can only update your own profile");
        }

        UserProfileResponse response = userService.updateUserProfile(instructorId, updateRequest);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/notifications")
    public ResponseEntity<List<NotificationResponse>> getAllNotifications() {
        UserAccount currentUser = userService.getCurrentUser();
        List<NotificationResponse> notifications = notificationService.getAllNotifications(currentUser.getUserAccountId());
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/notifications/unread")
    public ResponseEntity<List<NotificationResponse>> getUnreadNotifications() {
        UserAccount currentUser = userService.getCurrentUser();
        List<NotificationResponse> notifications = notificationService.getUnreadNotifications(currentUser.getUserAccountId());
        return ResponseEntity.ok(notifications);
    }

    @PutMapping("/notifications/mark-read/{notificationId}")
    public ResponseEntity<Void> markAsRead(@PathVariable Long notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/notifications/mark-all-read")
    public ResponseEntity<Void> markAllAsRead() {
        UserAccount currentUser = userService.getCurrentUser();
        notificationService.markAllAsRead(currentUser.getUserAccountId());
        return ResponseEntity.ok().build();
    }
}