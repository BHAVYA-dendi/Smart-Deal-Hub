package com.smartdealhub.smartdealhub.controller;

import com.smartdealhub.smartdealhub.model.Notification;
import com.smartdealhub.smartdealhub.model.User;
import com.smartdealhub.smartdealhub.repository.*;
import com.smartdealhub.smartdealhub.service.NotificationService;
import com.smartdealhub.smartdealhub.service.PushNotificationService;
import com.smartdealhub.smartdealhub.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

    private User requireCurrentUser(HttpServletRequest request) {
        Object cu = request.getAttribute("currentUser");
        if (cu instanceof User user) return user;
        throw new RuntimeException("Unauthenticated");
    }

    private boolean canAccessUser(User current, Long userId) {
        return current.getRole() == User.Role.ADMIN || current.getUserId().equals(userId);
    }

    private Notification requireOwnedNotification(Long notificationId, User current) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found with ID: " + notificationId));
        if (current.getRole() != User.Role.ADMIN
                && (notification.getUser() == null || !current.getUserId().equals(notification.getUser().getUserId()))) {
            throw new RuntimeException("Forbidden");
        }
        return notification;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Notification>> getNotificationsForUser(@PathVariable Long userId, HttpServletRequest request) {
        User current = requireCurrentUser(request);
        if (!canAccessUser(current, userId)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        return ResponseEntity.ok(notificationRepository.findByUser(user));
    }

    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<List<Notification>> getUnreadNotifications(@PathVariable Long userId, HttpServletRequest request) {
        User current = requireCurrentUser(request);
        if (!canAccessUser(current, userId)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(notificationService.getUnreadNotifications(userId));
    }

    @PostMapping("/send/user/{userId}")
    public ResponseEntity<Notification> sendNotificationToUser(
            @PathVariable Long userId,
            @RequestBody String message,
            HttpServletRequest request) {
        User current = requireCurrentUser(request);
        if (current.getRole() != User.Role.ADMIN) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        User user = userService.getUserById(userId);
        Notification notification = notificationService.createNotification(user, message);
        pushNotificationService.sendPush(user, notification);
        return ResponseEntity.ok(notification);
    }

    @PostMapping("/send/all")
    public ResponseEntity<String> sendNotificationToAll(@RequestBody String message, HttpServletRequest request) {
        User current = requireCurrentUser(request);
        if (current.getRole() != User.Role.ADMIN) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        List<User> users = userService.getAllUsers();
        for (User user : users) {
            Notification notification = notificationService.createNotification(user, message);
            pushNotificationService.sendPush(user, notification);
        }
        return ResponseEntity.ok("Notification sent to all users");
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<String> markAsRead(@PathVariable Long notificationId, HttpServletRequest request) {
        User current = requireCurrentUser(request);
        requireOwnedNotification(notificationId, current);
        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok("Notification marked as read");
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<String> deleteNotification(@PathVariable Long notificationId, HttpServletRequest request) {
        User current = requireCurrentUser(request);
        requireOwnedNotification(notificationId, current);
        notificationService.deleteNotification(notificationId);
        return ResponseEntity.ok("Notification deleted");
    }
}
