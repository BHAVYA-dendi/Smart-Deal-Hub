package com.smartdealhub.smartdealhub.controller;

import com.smartdealhub.smartdealhub.model.*;
import com.smartdealhub.smartdealhub.dto.*;
import com.smartdealhub.smartdealhub.repository.UserRepository;
import com.smartdealhub.smartdealhub.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final FavoriteService favoriteService;
    private final ReviewService reviewService;
    private final GroupDealService groupDealService;
    private final UserRepository userRepository;
    private final VisitedStoreService visitedStoreService;

    // ==============================
    // 🔹 STORE OWNER / ADMIN FEATURES
    // ==============================

    // Add a new product (Store Owner/Admin)
    @PostMapping("/store/{storeId}")
    public ResponseEntity<Product> addProduct(@PathVariable Long storeId, @RequestBody Product product) {
        return ResponseEntity.ok(productService.addProduct(storeId, product));
    }

    // Update product details (Store Owner/Admin)
    @PatchMapping("/{productId}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long productId, @RequestBody Product updatedProduct) {
        return ResponseEntity.ok(productService.updateProduct(productId, updatedProduct));
    }

    // Delete a product (Store Owner/Admin)
    @DeleteMapping("/{productId}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long productId) {
        productService.deleteProduct(productId);
        return ResponseEntity.ok("Product deleted successfully");
    }

    // ==============================
    // 🔹 USER FEATURES
    // ==============================

    // Get all products (lean DTO)
    @GetMapping
    public ResponseEntity<List<ProductSummaryDto>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts().stream()
                .map(ProductSummaryDto::from)
                .toList());
    }

    // Get product by ID (trimmed DTO to avoid recursive JSON)
    @GetMapping("/{productId}")
    public ResponseEntity<ProductSummaryDto> getProductById(
            @PathVariable Long productId,
            @RequestParam(required = false) Long userId) {

        Product product = productService.getProductById(productId);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }

        // Track visit (side-effect) if user provided
        if (userId != null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
            Store store = product.getStore();
            VisitedStore visit = new VisitedStore(user, store);
            visitedStoreService.addVisit(visit);
        }

        return ResponseEntity.ok(ProductSummaryDto.from(product));
    }

    // Search product by name (if no query provided, return all products) — returns DTOs
    @GetMapping("/search")
    public ResponseEntity<List<ProductSummaryDto>> searchProducts(@RequestParam(required = false) String query) {
        List<Product> list = (query == null || query.trim().isEmpty())
                ? productService.getAllProducts()
                : productService.searchProductsByName(query);
        return ResponseEntity.ok(list.stream().map(ProductSummaryDto::from).toList());
    }

    // Filter products by category or brand
    @GetMapping("/filter")
    public ResponseEntity<List<ProductSummaryDto>> filterProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String brand) {
        List<Product> list = productService.filterProducts(category, brand);
        return ResponseEntity.ok(list.stream().map(ProductSummaryDto::from).toList());
    }

    // Get products by tag
    @GetMapping("/tag/{tag}")
    public ResponseEntity<List<Product>> getProductsByTag(@PathVariable String tag) {
        return ResponseEntity.ok(productService.getProductsByTag(tag));
    }

    @GetMapping("/products/{productId}/compare")
    public ResponseEntity<ProductPriceComparisonResponse> compareProductPrices(
            @PathVariable Long productId,
            @RequestParam(required = false, defaultValue = "BOTH") String storeType,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice) {

        ProductPriceComparisonResponse response = productService.comparePrices(productId, storeType, minPrice, maxPrice);
        return ResponseEntity.ok(response);
    }

    // ==============================
    // 🔹 USER INTERACTIONS
    // ==============================

    // Add product to favorites
    @PostMapping("/{productId}/favorite/{userId}")
    public ResponseEntity<String> addFavorite(@PathVariable Long productId, @PathVariable Long userId) {
        favoriteService.addFavorite(userId, productId);
        return ResponseEntity.ok("Product added to favorites");
    }

    // Remove product from favorites
    @DeleteMapping("/{productId}/favorite/{userId}")
    public ResponseEntity<String> removeFavorite(@PathVariable Long productId, @PathVariable Long userId) {
        favoriteService.removeFavorite(userId, productId);
        return ResponseEntity.ok("Product removed from favorites");
    }

    // Add a review
    @PostMapping("/{productId}/review/{userId}")
    public ResponseEntity<Review> addReview(@PathVariable Long productId, @PathVariable Long userId,
                                            @RequestBody Review review) {
        return ResponseEntity.ok(reviewService.addReview(userId, productId, review));
    }

    // Get all reviews for a product
    @GetMapping("/{productId}/reviews")
    public ResponseEntity<List<Review>> getReviews(@PathVariable Long productId) {
        return ResponseEntity.ok(reviewService.getReviewsByProduct(productId));
    }

    // Lightweight DTO to avoid deep recursion in group deals
    private static class GroupDealSummary {
        public Long id;
        public Long productId;
        public String productName;
        public Integer memberCount;
        public String status;
        public Integer target;
        static GroupDealSummary from(GroupDeal d){
            GroupDealSummary s = new GroupDealSummary();
            s.id = d.getId();
            if (d.getProduct() != null){
                s.productId = d.getProduct().getId();
                s.productName = d.getProduct().getName();
                try { s.target = d.getProduct().getDealMaxMembers(); } catch(Exception ignored) {}
            }
            try { s.memberCount = (d.getMembers()!=null)? d.getMembers().size() : 0; } catch(Exception e){ s.memberCount = 0; }
            try { s.status = d.getStatus(); } catch(Exception ignored) {}
            return s;
        }
    }

    // Get active group deals for a product (trimmed summaries)
    @GetMapping("/{productId}/group-deals")
    public ResponseEntity<List<GroupDealSummary>> getGroupDeals(@PathVariable Long productId) {
        List<GroupDeal> deals = groupDealService.getActiveDealsByProduct(productId);
        List<GroupDealSummary> summaries = deals.stream().map(GroupDealSummary::from).toList();
        return ResponseEntity.ok(summaries);
    }
}