package com.smartdealhub.smartdealhub.service;

import com.smartdealhub.smartdealhub.model.Notification;
import com.smartdealhub.smartdealhub.model.User;
import com.smartdealhub.smartdealhub.repository.NotificationRepository;
import com.smartdealhub.smartdealhub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final PushNotificationService pushNotificationService;

    // ========================= SEND / CREATE =========================

    public void notifyUser(Long userId, String message) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage(message);
        notification.setReadStatus(false);
        notification.setCreatedAt(LocalDateTime.now());

        notificationRepository.save(notification);
        pushNotificationService.sendPush(user, notification);
    }

    public Notification createNotification(User user, String message) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage(message);
        notification.setReadStatus(false);
        notification.setCreatedAt(LocalDateTime.now());
        return notificationRepository.save(notification);
    }

    public Notification sendNotificationToUser(Long userId, String message) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        Notification notification = createNotification(user, message);
        pushNotificationService.sendPush(user, notification);
        return notification;
    }

    public void sendNotificationToAllUsers(String message) {
        List<User> users = userRepository.findAll();
        for (User user : users) {
            Notification notification = createNotification(user, message);
            pushNotificationService.sendPush(user, notification);
        }
    }

    // ========================= GET =========================

    public List<Notification> getNotifications(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        return notificationRepository.findByUser(user);
    }

    public List<Notification> getUnreadNotifications(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        return notificationRepository.findByUserAndReadStatusFalse(user);
    }

    // ========================= UPDATE / DELETE =========================

    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found with ID: " + notificationId));
        notification.setReadStatus(true);
        notificationRepository.save(notification);
    }

    public void deleteNotification(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found with ID: " + notificationId));
        notificationRepository.delete(notification);
    }

    // Convenience method for Admin to notify all users
    public void notifyAllUsers(String message) {
        sendNotificationToAllUsers(message);
    }


}