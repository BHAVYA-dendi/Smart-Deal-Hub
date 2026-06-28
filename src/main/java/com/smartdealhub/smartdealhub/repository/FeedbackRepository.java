package com.smartdealhub.smartdealhub.repository;

import com.smartdealhub.smartdealhub.model.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    // Get all feedback by a user
    List<Feedback> findByUserUserId(Long userId);

    // Get all feedback for a specific store
    List<Feedback> findByStoreStoreId(Long storeId);

    // Get top 10 recent feedback (for admin/dashboard)
    List<Feedback> findTop10ByOrderByCreatedAtDesc();
}