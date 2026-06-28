package com.smartdealhub.smartdealhub.service;

import com.smartdealhub.smartdealhub.model.Favorite;
import com.smartdealhub.smartdealhub.model.Product;
import com.smartdealhub.smartdealhub.model.User;
import com.smartdealhub.smartdealhub.repository.FavoriteRepository;
import com.smartdealhub.smartdealhub.repository.ProductRepository;
import com.smartdealhub.smartdealhub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    // ==============================
    // Add a product to user favorites
    // ==============================
    public Favorite addFavorite(Long userId, Long productId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Check if already exists
        Favorite existing = favoriteRepository.findByUserAndProduct(user, product);
        if (existing != null) return existing;

        Favorite favorite = new Favorite();
        favorite.setUser(user);
        favorite.setProduct(product);
        return favoriteRepository.save(favorite);
    }

    // ==============================
    // Remove a specific product from favorites
    // ==============================
    public void removeFavorite(Long userId, Long productId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Favorite favorite = favoriteRepository.findByUserAndProduct(user, product);
        if (favorite != null) favoriteRepository.delete(favorite);
    }

    // ==============================
    // Clear all favorites of a user
    // ==============================
    public void clearUserFavorites(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        favoriteRepository.deleteByUser(user);
    }

    // ==============================
    // Clear all favorites of a product
    // ==============================
    public void clearProductFavorites(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        favoriteRepository.deleteByProduct(product);
    }

    // ==============================
    // Get all favorites of a user
    // ==============================
    public List<Favorite> getFavoritesByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return favoriteRepository.findAllByUser(user);
    }

    // ==============================
    // Check if a product is a user favorite
    // ==============================
    public boolean isFavorite(Long userId, Long productId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Favorite favorite = favoriteRepository.findByUserAndProduct(user, product);
        return favorite != null;
    }

    // ==============================
    // Get favorite products of a store for a user
    // ==============================
    public List<Favorite> getFavoritesByStore(Long userId, Long storeId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return favoriteRepository.findByUserAndProduct_StoreStoreId(user, storeId);
    }
}