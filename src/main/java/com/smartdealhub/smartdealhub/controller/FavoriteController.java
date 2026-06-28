package com.smartdealhub.smartdealhub.controller;

import com.smartdealhub.smartdealhub.model.Favorite;
import com.smartdealhub.smartdealhub.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    // ==============================
    // Add product to favorites
    // ==============================
    @PostMapping(value = "/add", produces = org.springframework.http.MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> addFavorite(@RequestParam Long userId, @RequestParam Long productId) {
        favoriteService.addFavorite(userId, productId);
        return ResponseEntity.ok("Product added to favorites");
    }

    // ==============================
    // Remove a specific product from favorites
    // ==============================
    @DeleteMapping(value = "/remove", produces = org.springframework.http.MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> removeFavorite(@RequestParam Long userId, @RequestParam Long productId) {
        favoriteService.removeFavorite(userId, productId);
        return ResponseEntity.ok("Product removed from favorites successfully");
    }

    // ==============================
    // Delete all favorites of a user
    // ==============================
    @DeleteMapping("/user/{userId}/clear")
    public ResponseEntity<String> clearUserFavorites(@PathVariable Long userId) {
        favoriteService.clearUserFavorites(userId);
        return ResponseEntity.ok("All favorites cleared for the user");
    }

    // ==============================
    // Delete all favorites of a product
    // ==============================
    @DeleteMapping("/product/{productId}/clear")
    public ResponseEntity<String> clearProductFavorites(@PathVariable Long productId) {
        favoriteService.clearProductFavorites(productId);
        return ResponseEntity.ok("All favorites removed for the product");
    }

    // ==============================
    // Get all favorites of a user
    // ==============================
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Long>> getUserFavorites(@PathVariable Long userId) {
        List<Favorite> list = favoriteService.getFavoritesByUser(userId);
        List<Long> productIds = list.stream().map(f -> {
            try { return f.getProduct()!=null? f.getProduct().getId() : null; } catch(Exception e){ return null; }
        }).filter(java.util.Objects::nonNull).toList();
        return ResponseEntity.ok(productIds);
    }

    // ==============================
    // Check if a product is in user’s favorites
    // ==============================
    @GetMapping("/check")
    public ResponseEntity<Boolean> isFavorite(@RequestParam Long userId, @RequestParam Long productId) {
        boolean exists = favoriteService.isFavorite(userId, productId);
        return ResponseEntity.ok(exists);
    }

    // ==============================
    // Get favorite products of a store for a user
    // ==============================
    @GetMapping("/store/{storeId}/user/{userId}")
    public ResponseEntity<List<Favorite>> getStoreFavorites(@PathVariable Long storeId, @PathVariable Long userId) {
        return ResponseEntity.ok(favoriteService.getFavoritesByStore(userId, storeId));
    }
}