package com.smartdealhub.smartdealhub.controller;

import com.smartdealhub.smartdealhub.model.Review;
import com.smartdealhub.smartdealhub.model.User;
import com.smartdealhub.smartdealhub.repository.ReviewRepository;
import jakarta.servlet.http.HttpServletRequest;
import com.smartdealhub.smartdealhub.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final ReviewRepository reviewRepository;

    private User requireCurrentUser(HttpServletRequest request) {
        Object cu = request.getAttribute("currentUser");
        if (cu instanceof User user) return user;
        throw new RuntimeException("Unauthenticated");
    }

    // ==============================
    // 🔹 USER REVIEWS
    // ==============================

    // Add a review for a product (requires userId)
    @PostMapping("/product/{productId}/add")
    public ResponseEntity<Review> addReviewForProduct(
            @PathVariable Long productId,
            @RequestParam Long userId,
            @RequestBody Review review,
            HttpServletRequest request) {
        User current = requireCurrentUser(request);
        if (current.getRole() != User.Role.ADMIN && !current.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(reviewService.addReview(userId, productId, review));
    }

    // Update own review
    @PutMapping("/{reviewId}/update")
    public ResponseEntity<Review> updateReview(
            @PathVariable Long reviewId,
            @RequestBody Review updatedReview,
            HttpServletRequest request) {
        User current = requireCurrentUser(request);
        Review existing = reviewRepository.findById(reviewId).orElseThrow(() -> new RuntimeException("Review not found"));
        if (current.getRole() != User.Role.ADMIN &&
                (existing.getUser() == null || !current.getUserId().equals(existing.getUser().getUserId()))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(reviewService.updateReview(reviewId, updatedReview));
    }

    // Delete own review
    @DeleteMapping("/{reviewId}/delete")
    public ResponseEntity<String> deleteReview(@PathVariable Long reviewId, HttpServletRequest request) {
        User current = requireCurrentUser(request);
        Review existing = reviewRepository.findById(reviewId).orElseThrow(() -> new RuntimeException("Review not found"));
        if (current.getRole() != User.Role.ADMIN &&
                (existing.getUser() == null || !current.getUserId().equals(existing.getUser().getUserId()))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
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
            @RequestBody String reply,
            HttpServletRequest request) {
        User current = requireCurrentUser(request);
        if (current.getRole() != User.Role.ADMIN && current.getRole() != User.Role.STORE_OWNER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (current.getRole() == User.Role.STORE_OWNER) {
            Review existing = reviewRepository.findById(reviewId).orElseThrow(() -> new RuntimeException("Review not found"));
            boolean ownsProduct = existing.getProduct() != null &&
                    existing.getProduct().getStore() != null &&
                    existing.getProduct().getStore().getOwner() != null &&
                    current.getUserId().equals(existing.getProduct().getStore().getOwner().getUserId());
            if (!ownsProduct) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
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
    public ResponseEntity<List<Review>> getReviewsByUser(@PathVariable Long userId, HttpServletRequest request) {
        User current = requireCurrentUser(request);
        if (current.getRole() != User.Role.ADMIN && !current.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(reviewService.getReviewsByUser(userId));
    }
}