package com.smartdealhub.smartdealhub.controller;

import com.smartdealhub.smartdealhub.model.*;
import com.smartdealhub.smartdealhub.dto.*;
import com.smartdealhub.smartdealhub.repository.GroupMemberRepository;
import com.smartdealhub.smartdealhub.repository.ProductRepository;
import com.smartdealhub.smartdealhub.repository.StoreRepository;
import com.smartdealhub.smartdealhub.repository.UserRepository;
import com.smartdealhub.smartdealhub.service.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
    private final StoreRepository storeRepository;
    private final ProductRepository productRepository;

    private User requireCurrentUser(HttpServletRequest request) {
        Object cu = request.getAttribute("currentUser");
        if (cu instanceof User user) {
            return user;
        }
        throw new RuntimeException("Unauthenticated");
    }

    // ==============================
    // 🔹 STORE OWNER / ADMIN FEATURES
    // ==============================

    @PostMapping("/store/{storeId}")
    public ResponseEntity<Product> addProduct(@PathVariable Long storeId, @RequestBody Product product, HttpServletRequest request) {
        User current = requireCurrentUser(request);
        if (current.getRole() == User.Role.USER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Store store = storeRepository.findById(storeId).orElseThrow(() -> new RuntimeException("Store not found"));
        if (current.getRole() == User.Role.STORE_OWNER &&
                (store.getOwner() == null || !current.getUserId().equals(store.getOwner().getUserId()))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(productService.addProduct(storeId, product));
    }

    @PatchMapping("/{productId:\\d+}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long productId, @RequestBody Product updatedProduct, HttpServletRequest request) {
        User current = requireCurrentUser(request);
        Product existing = productRepository.findById(productId).orElseThrow(() -> new RuntimeException("Product not found"));
        if (current.getRole() == User.Role.USER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (current.getRole() == User.Role.STORE_OWNER &&
                (existing.getStore() == null || existing.getStore().getOwner() == null ||
                        !current.getUserId().equals(existing.getStore().getOwner().getUserId()))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(productService.updateProduct(productId, updatedProduct));
    }

    @DeleteMapping("/{productId:\\d+}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long productId, HttpServletRequest request) {
        User current = requireCurrentUser(request);
        Product existing = productRepository.findById(productId).orElseThrow(() -> new RuntimeException("Product not found"));
        if (current.getRole() == User.Role.USER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (current.getRole() == User.Role.STORE_OWNER &&
                (existing.getStore() == null || existing.getStore().getOwner() == null ||
                        !current.getUserId().equals(existing.getStore().getOwner().getUserId()))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        productService.deleteProduct(productId);
        return ResponseEntity.ok("Product deleted successfully");
    }

    // ==============================
    // 🔹 USER FEATURES (literal paths before /{productId})
    // ==============================

    @GetMapping
    public ResponseEntity<List<ProductSummaryDto>> getAllProducts() {
        try {
            return ResponseEntity.ok(productService.getAllProducts().stream()
                    .map(ProductSummaryDto::from)
                    .toList());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductSummaryDto>> searchProducts(@RequestParam(required = false) String query) {
        try {
            List<Product> list = productService.searchProductsByName(query);
            return ResponseEntity.ok(list.stream().map(ProductSummaryDto::from).toList());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/filter")
    public ResponseEntity<List<ProductSummaryDto>> filterProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String brand) {
        try {
            List<Product> list = productService.filterProducts(category, brand);
            return ResponseEntity.ok(list.stream().map(ProductSummaryDto::from).toList());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/compare")
    public ResponseEntity<ProductPriceComparisonResponse> compareByName(
            @RequestParam String name,
            @RequestParam(required = false, defaultValue = "BOTH") String type,
            @RequestParam(required = false) Double min,
            @RequestParam(required = false) Double max) {
        try {
            return ResponseEntity.ok(productService.compareProductsByName(name, type, min, max));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{productId:\\d+}/compare")
    public ResponseEntity<ProductPriceComparisonResponse> compareProductPrices(
            @PathVariable Long productId,
            @RequestParam(required = false, defaultValue = "BOTH") String storeType,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice) {
        try {
            return ResponseEntity.ok(productService.comparePrices(productId, storeType, minPrice, maxPrice));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/tag/{tag}")
    public ResponseEntity<List<ProductSummaryDto>> getProductsByTag(@PathVariable String tag) {
        try {
            return ResponseEntity.ok(productService.getProductsByTag(tag).stream()
                    .map(ProductSummaryDto::from)
                    .toList());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{productId:\\d+}")
    public ResponseEntity<ProductSummaryDto> getProductById(
            @PathVariable Long productId,
            @RequestParam(required = false) Long userId) {
        try {
            Product product = productService.getProductById(productId);
            if (userId != null) {
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
                Store store = product.getStore();
                if (store != null) {
                    VisitedStore visit = new VisitedStore(user, store);
                    visitedStoreService.addVisit(visit);
                }
            }
            return ResponseEntity.ok(ProductSummaryDto.from(product));
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==============================
    // 🔹 USER INTERACTIONS
    // ==============================

    @PostMapping("/{productId:\\d+}/favorite/{userId}")
    public ResponseEntity<String> addFavorite(@PathVariable Long productId, @PathVariable Long userId, HttpServletRequest request) {
        User current = requireCurrentUser(request);
        if (current.getRole() != User.Role.ADMIN && !current.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        favoriteService.addFavorite(userId, productId);
        return ResponseEntity.ok("Product added to favorites");
    }

    @DeleteMapping("/{productId:\\d+}/favorite/{userId}")
    public ResponseEntity<String> removeFavorite(@PathVariable Long productId, @PathVariable Long userId, HttpServletRequest request) {
        User current = requireCurrentUser(request);
        if (current.getRole() != User.Role.ADMIN && !current.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        favoriteService.removeFavorite(userId, productId);
        return ResponseEntity.ok("Product removed from favorites");
    }

    @PostMapping("/{productId:\\d+}/review/{userId}")
    public ResponseEntity<Review> addReview(@PathVariable Long productId, @PathVariable Long userId,
                                            @RequestBody Review review, HttpServletRequest request) {
        User current = requireCurrentUser(request);
        if (current.getRole() != User.Role.ADMIN && !current.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(reviewService.addReview(userId, productId, review));
    }

    @GetMapping("/{productId:\\d+}/reviews")
    public ResponseEntity<List<Review>> getReviews(@PathVariable Long productId) {
        return ResponseEntity.ok(reviewService.getReviewsByProduct(productId));
    }

    private static class GroupDealSummary {
        public Long id;
        public Long productId;
        public String productName;
        public Integer memberCount;
        public Integer currentMembers;
        public String status;
        public Integer target;

        static GroupDealSummary from(GroupDeal d, long totalMembers, long approvedMembers) {
            GroupDealSummary s = new GroupDealSummary();
            s.id = d.getId();
            if (d.getProduct() != null) {
                s.productId = d.getProduct().getId();
                s.productName = d.getProduct().getName();
                try { s.target = d.getProduct().getDealMaxMembers(); } catch (Exception ignored) {}
            }
            s.memberCount = (int) totalMembers;
            s.currentMembers = (int) approvedMembers;
            try { s.status = d.getStatus(); } catch (Exception ignored) {}
            return s;
        }
    }

    @GetMapping("/{productId:\\d+}/group-deals")
    public ResponseEntity<List<GroupDealSummary>> getGroupDeals(@PathVariable Long productId) {
        List<GroupDeal> deals = groupDealService.getActiveDealsByProduct(productId);
        List<GroupDealSummary> summaries = deals.stream().map(d -> {
            long total = d.getMembers() != null ? d.getMembers().size() : 0;
            long approved = d.getMembers() != null
                    ? d.getMembers().stream().filter(GroupMember::isApproved).count()
                    : 0;
            return GroupDealSummary.from(d, total, approved);
        }).toList();
        return ResponseEntity.ok(summaries);
    }
}
