package com.smartdealhub.smartdealhub.service;

import com.smartdealhub.smartdealhub.model.Notification;
import com.smartdealhub.smartdealhub.model.User;
import org.springframework.stereotype.Service;

@Service
public class PushNotificationService {

    public void sendPush(User user, Notification notification) {
        // TODO: Integrate with actual push notification provider (FCM, OneSignal, etc.)
        System.out.println("Push sent to user " + user.getUserId() + ": " + notification.getMessage());
    }
}