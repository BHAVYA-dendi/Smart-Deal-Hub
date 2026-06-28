package com.smartdealhub.smartdealhub.controller;

import com.smartdealhub.smartdealhub.model.Feedback;
import com.smartdealhub.smartdealhub.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List; 

@RestController
@RequestMapping("/api/feedbacks")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    // ==============================
    // Add feedback
    // ==============================
    @PostMapping("/add")
    public ResponseEntity<Feedback> addFeedback(@RequestBody Feedback feedback) {
        return ResponseEntity.ok(feedbackService.addFeedback(feedback));
    }

    // ==============================
    // Update feedback
    // ==============================
    @PutMapping("/{feedbackId}")
    public ResponseEntity<Feedback> updateFeedback(@PathVariable Long feedbackId, @RequestBody Feedback updatedFeedback) {
        return ResponseEntity.ok(feedbackService.updateFeedback(feedbackId, updatedFeedback));
    }

    // ==============================
    // Delete feedback
    // ==============================
    @DeleteMapping("/{feedbackId}")
    public ResponseEntity<String> deleteFeedback(@PathVariable Long feedbackId) {
        feedbackService.deleteFeedback(feedbackId);
        return ResponseEntity.ok("Feedback deleted successfully");
    }

    // ==============================
    // Get feedback by user
    // ==============================
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Feedback>> getFeedbackByUser(@PathVariable Long userId) {
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
    public ResponseEntity<List<Feedback>> getAllFeedback() {
        return ResponseEntity.ok(feedbackService.getAllFeedback());
    }

    // ==============================
    // Admin or Store Owner: Reply to feedback
    // ==============================
    @PostMapping("/{feedbackId}/reply")
    public ResponseEntity<Feedback> replyToFeedback(@PathVariable Long feedbackId, @RequestBody String reply) {
        return ResponseEntity.ok(feedbackService.replyToFeedback(feedbackId, reply));
    }

    // ==============================
    // Admin: Toggle feedback visibility
    // ==============================
    @PutMapping("/{feedbackId}/toggle-visibility")
    public ResponseEntity<String> toggleFeedbackVisibility(@PathVariable Long feedbackId) {
        feedbackService.toggleVisibility(feedbackId);
        return ResponseEntity.ok("Feedback visibility toggled");
    }
}