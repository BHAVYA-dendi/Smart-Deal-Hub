package com.smartdealhub.smartdealhub.service;

import com.smartdealhub.smartdealhub.model.Product;
import com.smartdealhub.smartdealhub.model.Review;
import com.smartdealhub.smartdealhub.model.User;
import com.smartdealhub.smartdealhub.repository.ProductRepository;
import com.smartdealhub.smartdealhub.repository.ReviewRepository;
import com.smartdealhub.smartdealhub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    // ========================= ADD / UPDATE / DELETE =========================

    // Add review for product by user
    public Review addReview(Long userId, Long productId, Review review) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));

        review.setUser(user);
        review.setProduct(product);
        review.setCreatedAt(LocalDateTime.now());

        return reviewRepository.save(review);
    }

    // Add review (used in ReviewController without userId param, assuming user already in Review object)
    public Review addReviewForProduct(Long productId, Review review) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));

        review.setProduct(product);
        review.setCreatedAt(LocalDateTime.now());
        return reviewRepository.save(review);
    }

    // Update review
    public Review updateReview(Long reviewId, Review updatedReview) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found with ID: " + reviewId));

        review.setRating(updatedReview.getRating());
        review.setComment(updatedReview.getComment());
        review.setCreatedAt(LocalDateTime.now());
        return reviewRepository.save(review);
    }

    // Delete review
    public void deleteReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found with ID: " + reviewId));
        reviewRepository.delete(review);
    }

    // ========================= STORE OWNER REPLY =========================

    public Review replyToReview(Long reviewId, String reply) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found with ID: " + reviewId));
        review.setOwnerReply(reply);
        return reviewRepository.save(review);
    }

    // ========================= GET REVIEWS =========================

    public List<Review> getReviewsByProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));
        return reviewRepository.findByProduct(product);
    }

    public List<Review> getReviewsByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        return reviewRepository.findByUser(user);
    }
}