package com.smartdealhub.smartdealhub.service;

import com.smartdealhub.smartdealhub.dto.*;
import com.smartdealhub.smartdealhub.model.*;
import com.smartdealhub.smartdealhub.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final FavoriteService favoriteService;
    private final ReviewService reviewService;
    private final GroupDealService groupDealService;
    private final PriceHistoryRepository priceHistoryRepository;

    // ================= STORE OWNER / ADMIN =================

    public Product addProduct(Long storeId, Product product) {
        Store store = new Store();
        store.setStoreId(storeId);
        product.setStore(store);
        product.setActive(true);
        product.setCreatedAt(LocalDateTime.now());
        return productRepository.save(product);
    }

    public Product updateProduct(Long productId, Product updatedProduct) {
        Product existing = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));

        existing.setName(updatedProduct.getName());
        existing.setDescription(updatedProduct.getDescription());
        existing.setCategory(updatedProduct.getCategory());
        existing.setBrand(updatedProduct.getBrand());

        // Track price change
        if (existing.getPrice() == null || !existing.getPrice().equals(updatedProduct.getPrice())) {
            PriceHistory history = new PriceHistory();
            history.setProduct(existing);
            history.setOldPrice(existing.getPrice() != null ? existing.getPrice().doubleValue() : 0.0);
            history.setNewPrice(updatedProduct.getPrice() != null ? updatedProduct.getPrice().doubleValue() : 0.0);
            history.setChangedAt(LocalDateTime.now());
            priceHistoryRepository.save(history);

            existing.setPrice(updatedProduct.getPrice());
        }

        existing.setActive(updatedProduct.getActive());
        existing.setGroupDealAllowed(updatedProduct.getGroupDealAllowed());
        existing.setImageUrl(updatedProduct.getImageUrl());

        return productRepository.save(existing);
    }

    public void deleteProduct(Long productId) {
        Product existing = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));
        productRepository.delete(existing);
    }

    // ================= USER FEATURES =================

    public List<Product> getAllProducts() {
        return productRepository.findAllWithStore();
    }

    public Product getProductById(Long productId) {
        return productRepository.findByIdWithStore(productId)
                .or(() -> productRepository.findById(productId))
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));
    }

    public List<Product> searchProductsByName(String query) {
        if (query == null || query.isBlank()) {
            return getAllProducts();
        }
        return productRepository.searchByNameWithStore(query.trim());
    }

    public List<Product> filterProducts(String category, String brand) {
        if (category != null && brand != null) {
            return productRepository.findByCategoryAndBrandWithStore(category, brand);
        } else if (category != null) {
            return productRepository.findByCategoryWithStore(category);
        } else if (brand != null) {
            return productRepository.findByBrandWithStore(brand);
        } else {
            return getAllProducts();
        }
    }

    public List<Product> getProductsByTag(String tag) {
        return productRepository.findByTagsContainingIgnoreCase(tag);
    }

    // ================= FAVORITES =================

    public void addFavorite(Long userId, Long productId) {
        favoriteService.addFavorite(userId, productId);
    }

    public void removeFavorite(Long userId, Long productId) {
        favoriteService.removeFavorite(userId, productId);
    }

    // ================= REVIEWS =================

    public List<Review> getReviews(Long productId) {
        return reviewService.getReviewsByProduct(productId);
    }

    // ================= GROUP DEALS =================

    public List<GroupDeal> getActiveGroupDeals(Long productId) {
        return groupDealService.getActiveDealsByProduct(productId);
    }

    // ================= PRICE COMPARISON =================

    public ProductPriceComparisonResponse comparePrices(Long productId, String storeType, Double minPrice, Double maxPrice) {
        Product product = getProductById(productId);
        return compareProductsByName(product.getName(), storeType, minPrice, maxPrice);
    }

    public ProductPriceComparisonResponse compareProductsByName(String name, String storeType, Double minPrice, Double maxPrice) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Product name is required");
        }
        List<Product> products = productRepository.searchByNameWithStore(name.trim());

        if (!"BOTH".equalsIgnoreCase(storeType != null ? storeType : "BOTH")) {
            String type = storeType.toUpperCase();
            products = products.stream()
                    .filter(p -> p.getStore() != null && p.getStore().getStoreType() != null
                            && type.equalsIgnoreCase(p.getStore().getStoreType().name()))
                    .collect(Collectors.toList());
        }

        if (minPrice != null) {
            products = products.stream()
                    .filter(p -> p.getPrice() != null && p.getPrice().compareTo(BigDecimal.valueOf(minPrice)) >= 0)
                    .collect(Collectors.toList());
        }
        if (maxPrice != null) {
            products = products.stream()
                    .filter(p -> p.getPrice() != null && p.getPrice().compareTo(BigDecimal.valueOf(maxPrice)) <= 0)
                    .collect(Collectors.toList());
        }

        List<ProductComparisonItem> items = products.stream()
                .filter(p -> p.getPrice() != null)
                .map(p -> new ProductComparisonItem(
                        p.getId(),
                        p.getName(),
                        p.getPrice().doubleValue(),
                        p.getStore() != null ? p.getStore().getName() : "Unknown",
                        p.getStore() != null ? p.getStore().getStoreType().name() : "UNKNOWN",
                        p.getStore() != null ? p.getStore().getCity() : "",
                        p.getStore() != null ? p.getStore().getState() : "",
                        p.getStore() != null ? p.getStore().getLatitude() : null,
                        p.getStore() != null ? p.getStore().getLongitude() : null
                ))
                .collect(Collectors.toList());

        return new ProductPriceComparisonResponse(name, items);
    }
}