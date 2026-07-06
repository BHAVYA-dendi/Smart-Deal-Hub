package com.smartdealhub.smartdealhub.controller;

import com.smartdealhub.smartdealhub.model.*;
import com.smartdealhub.smartdealhub.repository.*;
import com.smartdealhub.smartdealhub.service.StoreService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/owner/me")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class OwnerDashboardController {

    private final StoreService storeService;
    private final StoreRepository storeRepository;
    private final ProductRepository productRepository;
    private final OfferRepository offerRepository;
    private final ReviewRepository reviewRepository;
    private final GroupDealRepository groupDealRepository;

    private User getCurrent(HttpServletRequest req){
        Object u = req.getAttribute("currentUser");
        if(!(u instanceof User)) throw new RuntimeException("Unauthenticated");
        User user = (User) u;
        if (user.getRole() != User.Role.STORE_OWNER && user.getRole() != User.Role.ADMIN) {
            throw new RuntimeException("Forbidden: not a store owner");
        }
        if (user.getApprovalStatus() != User.ApprovalStatus.APPROVED) {
            throw new RuntimeException("Account is pending approval");
        }
        return user;
    }

    private Store requireOwnerStore(User owner){
        Store s = storeService.getOwnerSingleStore(owner);
        if(s == null) throw new RuntimeException("No store found for owner. Please create one.");
        return s;
    }

    private boolean ownsProduct(User owner, Product p){
        return p != null && p.getStore()!=null && p.getStore().getOwner()!=null &&
                owner.getUserId().equals(p.getStore().getOwner().getUserId());
    }

    private boolean ownsOffer(User owner, Offer o){
        return o != null && o.getProduct()!=null && ownsProduct(owner, o.getProduct());
    }

    // ===== Store setup / fetch =====

    @GetMapping("/store")
    public ResponseEntity<?> getMyStore(HttpServletRequest request){
        User owner = getCurrent(request);
        Store s = storeService.getOwnerSingleStore(owner);
        if(s == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message","NO_STORE"));
        return ResponseEntity.ok(s);
    }

    @PostMapping("/store")
    public ResponseEntity<Store> upsertMyStore(HttpServletRequest request, @RequestBody Store storePayload){
        User owner = getCurrent(request);
        Store saved = storeService.upsertOwnerStore(owner, storePayload);
        return ResponseEntity.ok(saved);
    }

    // ===== Products =====

    @GetMapping("/products")
    public ResponseEntity<List<Product>> myProducts(HttpServletRequest request){
        User owner = getCurrent(request);
        Store s = requireOwnerStore(owner);
        return ResponseEntity.ok(productRepository.findByStoreStoreId(s.getStoreId()));
    }

    @PostMapping("/products")
    public ResponseEntity<Product> addProduct(HttpServletRequest request, @RequestBody Product product){
        User owner = getCurrent(request);
        Store s = requireOwnerStore(owner);
        product.setStore(s); // auto attach owner store
        product.setActive(true);
        return ResponseEntity.ok(productRepository.save(product));
    }

    @DeleteMapping("/products/{productId}")
    public ResponseEntity<?> deleteProduct(HttpServletRequest request, @PathVariable Long productId){
        User owner = getCurrent(request);
        Product existing = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        if(!ownsProduct(owner, existing)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        productRepository.delete(existing);
        return ResponseEntity.ok(Map.of("message","deleted"));
    }

    // ===== Offers =====

    @GetMapping("/offers")
    public ResponseEntity<List<Offer>> myOffers(HttpServletRequest request){
        User owner = getCurrent(request);
        Store s = requireOwnerStore(owner);
        return ResponseEntity.ok(offerRepository.findByProductStoreId(s.getStoreId()));
    }

    @PostMapping("/offers/{productId}")
    public ResponseEntity<Offer> addOffer(HttpServletRequest request, @PathVariable Long productId, @RequestBody Offer offer){
        User owner = getCurrent(request);
        Product p = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        if(!ownsProduct(owner, p)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        offer.setProduct(p);
        if(offer.getApproved()==null) offer.setApproved(false);
        return ResponseEntity.ok(offerRepository.save(offer));
    }

    @DeleteMapping("/offers/{offerId}")
    public ResponseEntity<?> deleteOffer(HttpServletRequest request, @PathVariable Long offerId){
        User owner = getCurrent(request);
        Offer existing = offerRepository.findById(offerId)
                .orElseThrow(() -> new RuntimeException("Offer not found"));
        if(!ownsOffer(owner, existing)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        offerRepository.delete(existing);
        return ResponseEntity.ok(Map.of("message","deleted"));
    }

    // ===== Reviews: view + reply =====

    @GetMapping("/reviews")
    public ResponseEntity<List<Review>> myProductReviews(HttpServletRequest request){
        User owner = getCurrent(request);
        Store s = requireOwnerStore(owner);
        List<Product> products = productRepository.findByStoreStoreId(s.getStoreId());
        List<Review> out = new ArrayList<>();
        for(Product p: products){
            out.addAll(reviewRepository.findByProduct(p));
        }
        return ResponseEntity.ok(out);
    }

    @PutMapping("/reviews/{reviewId}/reply")
    public ResponseEntity<Review> replyToReview(HttpServletRequest request, @PathVariable Long reviewId, @RequestBody String reply){
        User owner = getCurrent(request);
        Review r = reviewRepository.findById(reviewId).orElseThrow(() -> new RuntimeException("Review not found"));
        if(!ownsProduct(owner, r.getProduct())) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        r.setOwnerReply(reply);
        return ResponseEntity.ok(reviewRepository.save(r));
    }

    // ===== Group deals: list + approve/reject =====

    @GetMapping("/group-deals")
    public ResponseEntity<List<Map<String, Object>>> getStoreGroupDeals(HttpServletRequest request) {
        try {
            User owner = getCurrent(request);
            Store store = requireOwnerStore(owner);

            // Get all products for the store
            List<Product> products = productRepository.findByStoreStoreId(store.getStoreId());
            List<Long> productIds = products.stream().map(Product::getId).collect(Collectors.toList());

            if (productIds.isEmpty()) {
                return ResponseEntity.ok(Collections.emptyList());
            }

            // Get all group deals for these products with members loaded (optimized)
            List<GroupDeal> deals = new ArrayList<>();
            for (Long pid : productIds) {
                List<GroupDeal> productDeals = groupDealRepository.findByProductIdWithMembers(pid);
                deals.addAll(productDeals);
            }

            // Convert to DTO to avoid circular references
            List<Map<String, Object>> result = deals.stream().map(deal -> {
                Map<String, Object> dto = new HashMap<>();
                dto.put("id", deal.getId());
                dto.put("title", deal.getTitle());
                dto.put("description", deal.getDescription());
                dto.put("status", deal.getStatus() != null ? deal.getStatus() : "PENDING");
                dto.put("createdAt", deal.getCreatedAt());
                dto.put("discountPercent", deal.getDiscountPercent());
                dto.put("maxMembers", deal.getMaxMembers());

                // Add product info
                if (deal.getProduct() != null) {
                    Map<String, Object> productInfo = new HashMap<>();
                    productInfo.put("id", deal.getProduct().getId());
                    productInfo.put("name", deal.getProduct().getName());
                    productInfo.put("price", deal.getProduct().getPrice());
                    dto.put("product", productInfo);
                }

                // Add initiator info
                if (deal.getInitiator() != null) {
                    Map<String, Object> initiatorInfo = new HashMap<>();
                    initiatorInfo.put("id", deal.getInitiator().getUserId());
                    initiatorInfo.put("name", deal.getInitiator().getName());
                    dto.put("initiator", initiatorInfo);
                }

                // Get member count from the loaded members collection
                int memberCount = 0;
                if (deal.getMembers() != null) {
                    memberCount = (int) deal.getMembers().stream()
                            .filter(GroupMember::isApproved)
                            .count();
                }
                dto.put("memberCount", memberCount);

                return dto;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonList(Collections.singletonMap("error", e.getMessage())));
        }
    }

    @PutMapping("/group-deals/{dealId}/status")
    public ResponseEntity<?> updateDealStatus(
            HttpServletRequest request,
            @PathVariable Long dealId,
            @RequestParam String status
    ) {
        try {
            User owner = getCurrent(request);
            GroupDeal deal = groupDealRepository.findById(dealId)
                    .orElseThrow(() -> new RuntimeException("Deal not found"));
            
            // Verify the deal is for a product owned by this store
            if (!productRepository.findById(deal.getProduct().getId())
                    .map(p -> p.getStore().getOwner().getUserId().equals(owner.getUserId()))
                    .orElse(false)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Collections.singletonMap("error", "Not authorized to update this deal"));
            }
            
            // Update status
            deal.setStatus(status.toUpperCase());
            GroupDeal updatedDeal = groupDealRepository.save(deal);
            
            // Fetch the deal again with members loaded
            updatedDeal = groupDealRepository.findById(dealId)
                    .orElseThrow(() -> new RuntimeException("Failed to fetch updated deal"));
            
            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("id", updatedDeal.getId());
            response.put("status", updatedDeal.getStatus());
            response.put("message", "Deal status updated successfully");
            
            // Add member count
            response.put("memberCount", updatedDeal.getMembers() != null ? updatedDeal.getMembers().size() : 0);
            
            // Add members info if available
            if (updatedDeal.getMembers() != null && !updatedDeal.getMembers().isEmpty()) {
                List<Map<String, Object>> members = updatedDeal.getMembers().stream()
                        .map(member -> {
                            Map<String, Object> m = new HashMap<>();
                            m.put("userId", member.getUser().getUserId());
                            m.put("username", member.getUser().getName());
                            m.put("joinedAt", member.getJoinedAt());
                            m.put("approved", member.isApproved());
                            return m;
                        })
                        .collect(Collectors.toList());
                response.put("members", members);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @GetMapping("/group-deals/{dealId}")
    public ResponseEntity<?> getDealDetails(
            HttpServletRequest request,
            @PathVariable Long dealId
    ) {
        try {
            User owner = getCurrent(request);
            GroupDeal deal = groupDealRepository.findByIdWithMembers(dealId)
                    .orElseThrow(() -> new RuntimeException("Deal not found"));
            
            // Verify the deal is for a product owned by this store
            if (!productRepository.findById(deal.getProduct().getId())
                    .map(p -> p.getStore().getOwner().getUserId().equals(owner.getUserId()))
                    .orElse(false)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Collections.singletonMap("error", "Not authorized to view this deal"));
            }
            
            // Build detailed response
            Map<String, Object> response = new HashMap<>();
            response.put("id", deal.getId());
            response.put("title", deal.getTitle());
            response.put("description", deal.getDescription());
            response.put("status", deal.getStatus());
            response.put("createdAt", deal.getCreatedAt());
            response.put("discountPercent", deal.getDiscountPercent());
            response.put("maxMembers", deal.getMaxMembers());
            
            // Product info
            if (deal.getProduct() != null) {
                Map<String, Object> productInfo = new HashMap<>();
                productInfo.put("id", deal.getProduct().getId());
                productInfo.put("name", deal.getProduct().getName());
                productInfo.put("price", deal.getProduct().getPrice());
                response.put("product", productInfo);
            }
            
            // Initiator info
            if (deal.getInitiator() != null) {
                Map<String, Object> initiatorInfo = new HashMap<>();
                initiatorInfo.put("id", deal.getInitiator().getUserId());
                initiatorInfo.put("name", deal.getInitiator().getName());
                response.put("initiator", initiatorInfo);
            }
            
            // Members info
            if (deal.getMembers() != null) {
                List<Map<String, Object>> members = deal.getMembers().stream()
                        .map(member -> {
                            Map<String, Object> m = new HashMap<>();
                            m.put("userId", member.getUser().getUserId());
                            m.put("username", member.getUser().getName());
                            m.put("joinedAt", member.getJoinedAt());
                            m.put("approved", member.isApproved());
                            return m;
                        })
                        .collect(Collectors.toList());
                response.put("members", members);
                response.put("memberCount", members.size());
            } else {
                response.put("members", Collections.emptyList());
                response.put("memberCount", 0);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", e.getMessage()));
        }
    }
}
