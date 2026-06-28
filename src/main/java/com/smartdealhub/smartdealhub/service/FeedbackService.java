package com.smartdealhub.smartdealhub.service;

import com.smartdealhub.smartdealhub.model.Feedback;
import com.smartdealhub.smartdealhub.model.User;
import com.smartdealhub.smartdealhub.repository.FeedbackRepository;
import com.smartdealhub.smartdealhub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    // ===== Add new feedback =====
    public Feedback addFeedback(Feedback feedback) {
        if (feedback.getUser() == null || feedback.getUser().getUserId() == null) {
            throw new IllegalArgumentException("Feedback must contain a valid user");
        }
        User user = userRepository.findById(feedback.getUser().getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + feedback.getUser().getUserId()));
        feedback.setUser(user);
        Feedback saved = feedbackRepository.save(feedback);
        notificationService.notifyUser(user.getUserId(), "Your feedback has been submitted successfully.");
        return saved;
    }

    // ===== Update feedback (message/subject/rating) =====
    public Feedback updateFeedback(Long feedbackId, Feedback updatedFeedback) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new IllegalArgumentException("Feedback not found with id: " + feedbackId));

        // Update fields
        feedback.setMessage(updatedFeedback.getMessage());
        feedback.setSubject(updatedFeedback.getSubject());
        feedback.setRating(updatedFeedback.getRating());

        return feedbackRepository.save(feedback);
    }

    // ===== Delete feedback =====
    public void deleteFeedback(Long feedbackId) {
        if (!feedbackRepository.existsById(feedbackId)) {
            throw new IllegalArgumentException("Feedback not found with id: " + feedbackId);
        }
        feedbackRepository.deleteById(feedbackId);
    }

    // ===== Get feedback by user =====
    public List<Feedback> getFeedbackByUser(Long userId) {
        return feedbackRepository.findByUserUserId(userId);
    }

    // ===== Get feedback by store (if Feedback linked to Product/Store) =====
    public List<Feedback> getFeedbackByStore(Long storeId) {
        return feedbackRepository.findByStoreStoreId(storeId); // Implement this in repository if needed
    }

    // ===== Get all feedback =====
    public List<Feedback> getAllFeedback() {
        return feedbackRepository.findAll();
    }

    // ===== Reply to feedback =====
    public Feedback replyToFeedback(Long feedbackId, String reply) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new IllegalArgumentException("Feedback not found with id: " + feedbackId));
        feedback.setReply(reply);
        return feedbackRepository.save(feedback);
    }

    // ===== Toggle visibility =====
    public Feedback toggleVisibility(Long feedbackId) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new IllegalArgumentException("Feedback not found with id: " + feedbackId));
        feedback.setVisible(!feedback.getVisible());
        return feedbackRepository.save(feedback);
    }
}