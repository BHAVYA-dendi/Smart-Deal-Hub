package com.smartdealhub.smartdealhub.service;

import com.smartdealhub.smartdealhub.model.User;
import com.smartdealhub.smartdealhub.model.UserActivity;
import com.smartdealhub.smartdealhub.repository.UserActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final UserActivityRepository repository;
    private final UserService userService; // make sure you have this injected

    // Record activity by userId
    public void recordActivity(Long userId, UserActivity.ActivityType type,
                               Long referenceId, String referenceType,
                               String description) {
        User user = userService.getUserById(userId);
        if (user == null) return;

        UserActivity activity = UserActivity.builder()
                .user(user)
                .activityType(type)
                .referenceId(referenceId)
                .referenceType(referenceType)
                .description(description)
                .createdAt(LocalDateTime.now())
                .build();
        repository.save(activity);
    }

    // Record activity by userEmail
    public void recordActivity(String userEmail, UserActivity.ActivityType type,
                               Long referenceId, String referenceType,
                               String description) {
        User user = userService.getUserByEmail(userEmail);
        if (user == null) return;

        recordActivity(user.getUserId(), type, referenceId, referenceType, description);
    }

    // Get all activities for a user
    public List<UserActivity> getActivitiesByUserId(Long userId) {
        return repository.findByUser_UserIdOrderByCreatedAtDesc(userId);
    }
}