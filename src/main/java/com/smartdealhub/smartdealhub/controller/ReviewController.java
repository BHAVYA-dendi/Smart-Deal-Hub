package com.smartdealhub.smartdealhub.controller;

import com.smartdealhub.smartdealhub.model.Review;
import com.smartdealhub.smartdealhub.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // ==============================
    // 🔹 USER REVIEWS
    // ==============================

    // Add a review for a product (requires userId)
    @PostMapping("/product/{productId}/add")
    public ResponseEntity<Review> addReviewForProduct(
            @PathVariable Long productId,
            @RequestParam Long userId,
            @RequestBody Review review) {
        return ResponseEntity.ok(reviewService.addReview(userId, productId, review));
    }

    // Update own review
    @PutMapping("/{reviewId}/update")
    public ResponseEntity<Review> updateReview(
            @PathVariable Long reviewId,
            @RequestBody Review updatedReview) {
        return ResponseEntity.ok(reviewService.updateReview(reviewId, updatedReview));
    }

    // Delete own review
    @DeleteMapping("/{reviewId}/delete")
    public ResponseEntity<String> deleteReview(@PathVariable Long reviewId) {
        reviewService.deleteReview(reviewId);
        return ResponseEntity.ok("Review deleted successfully");
    }

    // ==============================
    // 🔹 STORE OWNER REPLY
    // ==============================

    // Store owner replies to a review
    @PutMapping("/{reviewId}/reply")
    public ResponseEntity<Review> replyToReview(
            @PathVariable Long reviewId,
            @RequestBody String reply) {
        return ResponseEntity.ok(reviewService.replyToReview(reviewId, reply));
    }

    // ==============================
    // 🔹 GET REVIEWS
    // ==============================

    // Get all reviews for a product
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<Review>> getReviewsForProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(reviewService.getReviewsByProduct(productId));
    }

    // Get all reviews by a user
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Review>> getReviewsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(reviewService.getReviewsByUser(userId));
    }
}