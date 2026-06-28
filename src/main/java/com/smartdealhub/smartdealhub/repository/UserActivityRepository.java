package com.smartdealhub.smartdealhub.repository;

import com.smartdealhub.smartdealhub.model.UserActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserActivityRepository extends JpaRepository<UserActivity, Long> {

    List<UserActivity> findByUser_UserIdOrderByCreatedAtDesc(Long userId);
}