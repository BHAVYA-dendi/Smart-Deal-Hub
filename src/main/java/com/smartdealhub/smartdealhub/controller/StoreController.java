
package com.smartdealhub.smartdealhub.controller;

import com.smartdealhub.smartdealhub.dto.NotificationRequest;
import com.smartdealhub.smartdealhub.dto.RouteOptimizationRequest;
import com.smartdealhub.smartdealhub.dto.RouteStep;
import com.smartdealhub.smartdealhub.dto.RouteResponse;
import com.smartdealhub.smartdealhub.model.*;
import com.smartdealhub.smartdealhub.repository.ProductRepository;
import com.smartdealhub.smartdealhub.repository.UserRepository;
import com.smartdealhub.smartdealhub.repository.StoreRepository;
import com.smartdealhub.smartdealhub.service.NotificationService;
import com.smartdealhub.smartdealhub.service.StoreHourService;
import com.smartdealhub.smartdealhub.service.VisitedStoreService;
import com.smartdealhub.smartdealhub.service.StoreService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/stores")
public class StoreController {

    private final StoreRepository storeRepository;
    private final ProductRepository productRepository;
    private final NotificationService notificationService;
    private final StoreHourService storeHourService;
    private VisitedStoreService visitedStoreService;
    private UserRepository userRepository;
    private final StoreService storeService;

    public StoreController(StoreRepository storeRepository,
                           ProductRepository productRepository,
                           NotificationService notificationService,
                           StoreHourService storeHourService,
                           VisitedStoreService visitedStoreService,
                           UserRepository userRepository,
                           StoreService storeService
    ) {
        this.storeRepository = storeRepository;
        this.productRepository = productRepository;
        this.notificationService = notificationService;
        this.storeHourService = storeHourService;
        this.visitedStoreService=visitedStoreService;
        this.userRepository=userRepository;
        this.storeService = storeService;
    }

    // ================= CRUD =================
    @PostMapping("/add")
    public ResponseEntity<?> addStore(@RequestHeader("role") String role, @RequestBody Store store) {
        if (!"ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only ADMIN can add stores");
        }
        try {
            storeRepository.save(store);
            return ResponseEntity.status(HttpStatus.CREATED).body(store);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to add store", "details", e.getMessage()));
        }
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateStore(@RequestHeader("role") String role, @RequestBody Store store) {
        if (!"ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only ADMIN can update stores");
        }
        try {
            storeRepository.save(store);
            return ResponseEntity.ok(store);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update store", "details", e.getMessage()));
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteStore(@RequestHeader("role") String role, @PathVariable Long id) {
        if (!"ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only ADMIN can delete stores");
        }
        try {
            storeRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "Store deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete store", "details", e.getMessage()));
        }
    }

    @GetMapping("/{storeId}")
    public ResponseEntity<Store> getStore(
            @PathVariable Long storeId,
            @RequestParam Long userId) {

        // Fetch store
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found with ID: " + storeId));

        // Fetch user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        // Automatically add visit
        VisitedStore visit = new VisitedStore(user, store);
        visitedStoreService.addVisit(visit);

        return ResponseEntity.ok(store);
    }

    @GetMapping("/all")
    public List<Store> getAllStores() {
        return storeService.getAllStores();
    }

    // ================= Search & Filter =================
    @GetMapping("/city/{city}")
    public List<Store> getStoresByCity(@PathVariable String city) {
        return storeRepository.findByCity(city);
    }

    @GetMapping("/state/{state}")
    public List<Store> getStoresByState(@PathVariable String state) {
        return storeRepository.findByState(state);
    }

    @GetMapping("/type/{type}")
    public List<Store> getStoresByType(@PathVariable String type) {
        return storeRepository.findByStoreType(type);
    }

    @GetMapping("/nearby/{lat}/{lon}/{radiusKm}")
    public List<Store> getNearbyStores(
            @PathVariable double lat,
            @PathVariable double lon,
            @PathVariable double radiusKm) {
        return storeRepository.findNearbyStores(lat, lon, radiusKm).stream()
                .sorted(Comparator.comparingDouble(s -> distanceInKm(lat, lon, s.getLatitude(), s.getLongitude())))
                .toList();
    }

    private double distanceInKm(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    // ================= Products in Store =================
    @GetMapping("/{id}/products")
    public List<Product> getProductsInStore(@PathVariable long id) {
        List<Product> list = productRepository.findByStoreStoreId(id);
        // Trim recursive fields to avoid infinite owner->stores loops during JSON serialization
        for (Product p : list) {
            try {
                // Break back-references that cause deep recursion
                if (p.getStore() != null) {
                    if (p.getStore().getOwner() != null) {
                        try { p.getStore().getOwner().setStores(null); } catch(Exception ignored) {}
                        try { p.getStore().getOwner().setPassword(null); } catch(Exception ignored) {}
                        try { p.getStore().getOwner().setFavorites(null); } catch(Exception ignored) {}
                        try { p.getStore().getOwner().setGroupDealsInitiated(null); } catch(Exception ignored) {}
                        try { p.getStore().getOwner().setGroupMemberships(null); } catch(Exception ignored) {}
                        try { p.getStore().getOwner().setNotifications(null); } catch(Exception ignored) {}
                        try { p.getStore().getOwner().setFeedbacks(null); } catch(Exception ignored) {}
                        try { p.getStore().getOwner().setVisitedStores(null); } catch(Exception ignored) {}
                        try { p.getStore().getOwner().setReviews(null); } catch(Exception ignored) {}
                        try { p.getStore().getOwner().setSentInvites(null); } catch(Exception ignored) {}
                        try { p.getStore().getOwner().setReceivedInvites(null); } catch(Exception ignored) {}
                    }
                    try { p.getStore().setProducts(null); } catch(Exception ignored) {}
                    try { p.getStore().setStoreHours(null); } catch(Exception ignored) {}
                    try { p.getStore().setFavorites(null); } catch(Exception ignored) {}
                    try { p.getStore().setVisitedStores(null); } catch(Exception ignored) {}
                }
                // Remove heavy collections on product
                try { p.setOffers(null); } catch(Exception ignored) {}
                try { p.setReviews(null); } catch(Exception ignored) {}
                try { p.setPriceHistoryList(null); } catch(Exception ignored) {}
                try { p.setGroupDeals(null); } catch(Exception ignored) {}
            } catch (Exception ignored) {}
        }
        return list;
    }

    @GetMapping("/{id}/products/category/{category}")
    public List<Product> getProductsByCategory(@PathVariable long id, @PathVariable String category) {
        return productRepository.findByStoreStoreIdAndCategory(id, category);
    }

    // ================= Store Hours =================
    @GetMapping("/id/{storeId}/hours")
    public List<StoreHour> getStoreHours(@PathVariable int storeId) {
        return storeHourService.getStoreHours(storeId);
    }

    @GetMapping("/name/{storeName}/hours")
    public List<StoreHour> getStoreHoursByName(@PathVariable String storeName) {
        return storeHourService.getStoreHoursByName(storeName);
    }@PostMapping("/{storeId}/hours/add")
    public ResponseEntity<?> addStoreHour(
            @RequestHeader("role") String role,
            @PathVariable Long storeId,
            @RequestBody StoreHour storeHour) {

        if (!"ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only ADMIN can add store hours"));
        }

        // Fetch the store object first
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found with ID: " + storeId));

        storeHour.setStore(store); // Set the actual Store object

        try {
            StoreHour savedHour = storeHourService.addStoreHour(storeHour);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedHour);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to add store hour", "details", e.getMessage()));
        }
    }

    @PutMapping("/{storeId}/hours/update")
    public ResponseEntity<?> updateStoreHour(
            @RequestHeader("role") String role,
            @PathVariable Long storeId,
            @RequestBody StoreHour storeHour) {

        if (!"ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only ADMIN can update store hours"));
        }

        // Fetch the store object first
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found with ID: " + storeId));

        storeHour.setStore(store); // Set the actual Store object

        try {
            StoreHour updatedHour = storeHourService.updateStoreHour(storeHour);
            return ResponseEntity.ok(updatedHour);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update store hour", "details", e.getMessage()));
        }
    }

    // ================= Notifications =================
    @PostMapping("/notify")
    public ResponseEntity<?> notifyStoreUsers(@RequestHeader("role") String role, @RequestBody NotificationRequest request) {
        if (!"ADMIN".equalsIgnoreCase(role)) return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only ADMIN can send notifications");
        notificationService.notifyAllUsers(request.getMessage());
        return ResponseEntity.ok("Notifications sent to all users for store ID " + request.getStoreId());
    }

    // ================= Route Optimization =================
    @PostMapping("/route/optimize")
    public RouteResponse optimizeRoute(@RequestBody RouteOptimizationRequest request) {
        List<Store> stores = storeRepository.findAllByIds(request.getStoreIds());
        double currentLat = 0, currentLon = 0;
        if (request.getStartLocation() != null && !request.getStartLocation().isEmpty()) {
            String[] parts = request.getStartLocation().split(",");
            currentLat = Double.parseDouble(parts[0]);
            currentLon = Double.parseDouble(parts[1]);
        }

        List<Store> remaining = new ArrayList<>(stores);
        List<RouteStep> steps = new ArrayList<>();
        List<String> routeIds = new ArrayList<>();
        routeIds.add("Current Location");

        while (!remaining.isEmpty()) {
            final double lat = currentLat;
            final double lon = currentLon;
            Store nearest = remaining.stream()
                    .min(Comparator.comparingDouble(s -> distanceInKm(lat, lon, s.getLatitude(), s.getLongitude())))
                    .get();

            double distance = distanceInKm(currentLat, currentLon, nearest.getLatitude(), nearest.getLongitude());
            steps.add(new RouteStep(nearest.getStoreId(), nearest.getName(), nearest.getLatitude(), nearest.getLongitude(), distance));

            currentLat = nearest.getLatitude();
            currentLon = nearest.getLongitude();
            routeIds.add("Store " + nearest.getStoreId());
            remaining.remove(nearest);
        }

        String routePath = String.join(" -> ", routeIds);
        return new RouteResponse(routePath, steps);
    }
    @GetMapping("/route")
    public ResponseEntity<RouteResponse> getStoreRoute() {
        List<RouteStep> steps = new ArrayList<>();
        steps.add(new RouteStep(1L, "Product A", 17.385, 78.4867, 499.0));
        steps.add(new RouteStep(2L, "Product B", 17.400, 78.5000, 599.0));

        RouteResponse response = new RouteResponse(steps, 12.5, "2 stores found along the route");

        return ResponseEntity.ok(response);
    }
}