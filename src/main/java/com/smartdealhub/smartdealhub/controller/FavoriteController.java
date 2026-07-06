package com.smartdealhub.smartdealhub.controller;

import com.smartdealhub.smartdealhub.model.Favorite;
import com.smartdealhub.smartdealhub.model.User;
import jakarta.servlet.http.HttpServletRequest;
import com.smartdealhub.smartdealhub.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;
    private User requireCurrentUser(HttpServletRequest request) {
        Object cu = request.getAttribute("currentUser");
        if (cu instanceof User user) return user;
        throw new RuntimeException("Unauthenticated");
    }

    private boolean canAccessUser(User current, Long userId) {
        return current.getRole() == User.Role.ADMIN || current.getUserId().equals(userId);
    }

    // ==============================
    // Add product to favorites
    // ==============================
    @PostMapping(value = "/add", produces = org.springframework.http.MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> addFavorite(@RequestParam Long userId, @RequestParam Long productId, HttpServletRequest request) {
        User current = requireCurrentUser(request);
        if (!canAccessUser(current, userId)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        favoriteService.addFavorite(userId, productId);
        return ResponseEntity.ok("Product added to favorites");
    }

    // ==============================
    // Remove a specific product from favorites
    // ==============================
    @DeleteMapping(value = "/remove", produces = org.springframework.http.MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> removeFavorite(@RequestParam Long userId, @RequestParam Long productId, HttpServletRequest request) {
        User current = requireCurrentUser(request);
        if (!canAccessUser(current, userId)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        favoriteService.removeFavorite(userId, productId);
        return ResponseEntity.ok("Product removed from favorites successfully");
    }

    // ==============================
    // Delete all favorites of a user
    // ==============================
    @DeleteMapping("/user/{userId}/clear")
    public ResponseEntity<String> clearUserFavorites(@PathVariable Long userId, HttpServletRequest request) {
        User current = requireCurrentUser(request);
        if (!canAccessUser(current, userId)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        favoriteService.clearUserFavorites(userId);
        return ResponseEntity.ok("All favorites cleared for the user");
    }

    // ==============================
    // Delete all favorites of a product
    // ==============================
    @DeleteMapping("/product/{productId}/clear")
    public ResponseEntity<String> clearProductFavorites(@PathVariable Long productId, HttpServletRequest request) {
        User current = requireCurrentUser(request);
        if (current.getRole() != User.Role.ADMIN) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        favoriteService.clearProductFavorites(productId);
        return ResponseEntity.ok("All favorites removed for the product");
    }

    // ==============================
    // Get all favorites of a user
    // ==============================
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Long>> getUserFavorites(@PathVariable Long userId, HttpServletRequest request) {
        User current = requireCurrentUser(request);
        if (!canAccessUser(current, userId)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
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
    public ResponseEntity<Boolean> isFavorite(@RequestParam Long userId, @RequestParam Long productId, HttpServletRequest request) {
        User current = requireCurrentUser(request);
        if (!canAccessUser(current, userId)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        boolean exists = favoriteService.isFavorite(userId, productId);
        return ResponseEntity.ok(exists);
    }

    // ==============================
    // Get favorite products of a store for a user
    // ==============================
    @GetMapping("/store/{storeId}/user/{userId}")
    public ResponseEntity<List<Favorite>> getStoreFavorites(@PathVariable Long storeId, @PathVariable Long userId, HttpServletRequest request) {
        User current = requireCurrentUser(request);
        if (!canAccessUser(current, userId)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(favoriteService.getFavoritesByStore(userId, storeId));
    }
}