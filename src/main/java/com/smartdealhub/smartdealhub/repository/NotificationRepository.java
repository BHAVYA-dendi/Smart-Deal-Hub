package com.smartdealhub.smartdealhub.repository;

import com.smartdealhub.smartdealhub.model.Notification;
import com.smartdealhub.smartdealhub.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // All notifications for a user
    List<Notification> findByUser(User user);

    // Only unread notifications
    List<Notification> findByUserAndReadStatusFalse(User user);



}