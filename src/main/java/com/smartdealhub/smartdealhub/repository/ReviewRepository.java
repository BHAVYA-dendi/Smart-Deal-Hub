package com.smartdealhub.smartdealhub.repository;

import com.smartdealhub.smartdealhub.model.Review;
import com.smartdealhub.smartdealhub.model.Product;
import com.smartdealhub.smartdealhub.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Find all reviews for a given product
    List<Review> findByProduct(Product product);

    // Find all reviews written by a given user
    List<Review> findByUser(User user);
}