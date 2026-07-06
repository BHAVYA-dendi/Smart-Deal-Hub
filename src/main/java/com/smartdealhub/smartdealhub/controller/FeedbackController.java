package com.smartdealhub.smartdealhub.controller;

import com.smartdealhub.smartdealhub.model.Feedback;
import com.smartdealhub.smartdealhub.model.User;
import com.smartdealhub.smartdealhub.repository.FeedbackRepository;
import jakarta.servlet.http.HttpServletRequest;
import com.smartdealhub.smartdealhub.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List; 

@RestController
@RequestMapping("/api/feedbacks")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;
    private final FeedbackRepository feedbackRepository;

    private User requireCurrentUser(HttpServletRequest request) {
        Object cu = request.getAttribute("currentUser");
        if (cu instanceof User user) return user;
        throw new RuntimeException("Unauthenticated");
    }

    // ==============================
    // Add feedback
    // ==============================
    @PostMapping("/add")
    public ResponseEntity<Feedback> addFeedback(@RequestBody Feedback feedback, HttpServletRequest request) {
        User current = requireCurrentUser(request);
        if (feedback.getUser() == null || feedback.getUser().getUserId() == null) {
            return ResponseEntity.badRequest().build();
        }
        if (current.getRole() != User.Role.ADMIN && !current.getUserId().equals(feedback.getUser().getUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(feedbackService.addFeedback(feedback));
    }

    // ==============================
    // Update feedback
    // ==============================
    @PutMapping("/{feedbackId}")
    public ResponseEntity<Feedback> updateFeedback(@PathVariable Long feedbackId, @RequestBody Feedback updatedFeedback, HttpServletRequest request) {
        User current = requireCurrentUser(request);
        Feedback existing = feedbackRepository.findById(feedbackId).orElseThrow(() -> new RuntimeException("Feedback not found"));
        if (current.getRole() != User.Role.ADMIN &&
                (existing.getUser() == null || !current.getUserId().equals(existing.getUser().getUserId()))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(feedbackService.updateFeedback(feedbackId, updatedFeedback));
    }

    // ==============================
    // Delete feedback
    // ==============================
    @DeleteMapping("/{feedbackId}")
    public ResponseEntity<String> deleteFeedback(@PathVariable Long feedbackId, HttpServletRequest request) {
        User current = requireCurrentUser(request);
        Feedback existing = feedbackRepository.findById(feedbackId).orElseThrow(() -> new RuntimeException("Feedback not found"));
        if (current.getRole() != User.Role.ADMIN &&
                (existing.getUser() == null || !current.getUserId().equals(existing.getUser().getUserId()))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        feedbackService.deleteFeedback(feedbackId);
        return ResponseEntity.ok("Feedback deleted successfully");
    }

    // ==============================
    // Get feedback by user
    // ==============================
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Feedback>> getFeedbackByUser(@PathVariable Long userId, HttpServletRequest request) {
        User current = requireCurrentUser(request);
        if (current.getRole() != User.Role.ADMIN && !current.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(feedbackService.getFeedbackByUser(userId));
    }

    // ==============================
    // Get feedback by store
    // ==============================
    @GetMapping("/store/{storeId}")
    public ResponseEntity<List<Feedback>> getFeedbackByStore(@PathVariable Long storeId) {
        return ResponseEntity.ok(feedbackService.getFeedbackByStore(storeId));
    }

    // ==============================
    // Admin: Get all feedback
    // ==============================
    @GetMapping("/all")
    public ResponseEntity<List<Feedback>> getAllFeedback(HttpServletRequest request) {
        User current = requireCurrentUser(request);
        if (current.getRole() != User.Role.ADMIN) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(feedbackService.getAllFeedback());
    }

    // ==============================
    // Admin or Store Owner: Reply to feedback
    // ==============================
    @PostMapping("/{feedbackId}/reply")
    public ResponseEntity<Feedback> replyToFeedback(@PathVariable Long feedbackId, @RequestBody String reply, HttpServletRequest request) {
        User current = requireCurrentUser(request);
        if (current.getRole() != User.Role.ADMIN && current.getRole() != User.Role.STORE_OWNER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (current.getRole() == User.Role.STORE_OWNER) {
            Feedback existing = feedbackRepository.findById(feedbackId).orElseThrow(() -> new RuntimeException("Feedback not found"));
            boolean ownsStore = existing.getStore() != null &&
                    existing.getStore().getOwner() != null &&
                    current.getUserId().equals(existing.getStore().getOwner().getUserId());
            if (!ownsStore) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(feedbackService.replyToFeedback(feedbackId, reply));
    }

    // ==============================
    // Admin: Toggle feedback visibility
    // ==============================
    @PutMapping("/{feedbackId}/toggle-visibility")
    public ResponseEntity<String> toggleFeedbackVisibility(@PathVariable Long feedbackId, HttpServletRequest request) {
        User current = requireCurrentUser(request);
        if (current.getRole() != User.Role.ADMIN) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        feedbackService.toggleVisibility(feedbackId);
        return ResponseEntity.ok("Feedback visibility toggled");
    }
}