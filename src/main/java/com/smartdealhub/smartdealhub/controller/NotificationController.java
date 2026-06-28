package com.smartdealhub.smartdealhub.controller;

import com.smartdealhub.smartdealhub.model.Notification;
import com.smartdealhub.smartdealhub.model.User;
import com.smartdealhub.smartdealhub.repository.*;
import com.smartdealhub.smartdealhub.service.NotificationService;
import com.smartdealhub.smartdealhub.service.PushNotificationService;
import com.smartdealhub.smartdealhub.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final PushNotificationService pushNotificationService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    // ============================
    // 🔹 GET NOTIFICATIONS
    // ============================

    // Get all notifications for a user
    @GetMapping("/user/{userId}")
    public List<Notification> getNotificationsForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        return notificationRepository.findByUser(user);
    }

    // Get only unread notifications for a user
    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<List<Notification>> getUnreadNotifications(@PathVariable Long userId) {
        List<Notification> notifications = notificationService.getUnreadNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    // ============================
    // 🔹 CREATE / SEND NOTIFICATIONS
    // ============================

    // Send notification to a single user (user, store owner, or admin)
    @PostMapping("/send/user/{userId}")
    public ResponseEntity<Notification> sendNotificationToUser(
            @PathVariable Long userId,
            @RequestBody String message) {

        User user = userService.getUserById(userId);
        Notification notification = notificationService.createNotification(user, message);
        pushNotificationService.sendPush(user, notification);

        return ResponseEntity.ok(notification);
    }

    // Send notification to multiple users (admin feature)
    @PostMapping("/send/all")
    public ResponseEntity<String> sendNotificationToAll(@RequestBody String message) {
        List<User> users = userService.getAllUsers();
        for (User user : users) {
            Notification notification = notificationService.createNotification(user, message);
            pushNotificationService.sendPush(user, notification);
        }
        return ResponseEntity.ok("Notification sent to all users");
    }



    // ============================
    // 🔹 UPDATE NOTIFICATIONS
    // ============================

    // Mark a notification as read
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<String> markAsRead(@PathVariable Long notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok("Notification marked as read");
    }

    // Delete a notification
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<String> deleteNotification(@PathVariable Long notificationId) {
        notificationService.deleteNotification(notificationId);
        return ResponseEntity.ok("Notification deleted");
    }
}