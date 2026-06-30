package com.smartdealhub.smartdealhub.controller;

import com.smartdealhub.smartdealhub.model.Offer;
import com.smartdealhub.smartdealhub.model.Product;
import com.smartdealhub.smartdealhub.model.User;
import com.smartdealhub.smartdealhub.repository.OfferRepository;
import com.smartdealhub.smartdealhub.repository.ProductRepository;
import com.smartdealhub.smartdealhub.service.OfferService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/offers")
@RequiredArgsConstructor
public class OfferController {

    private final OfferService offerService;
    private final OfferRepository offerRepository;
    private final ProductRepository productRepository;

    private User requireCurrentUser(HttpServletRequest request) {
        Object cu = request.getAttribute("currentUser");
        if (cu instanceof User user) return user;
        throw new RuntimeException("Unauthenticated");
    }

    private boolean ownsProduct(User current, Product product) {
        return product != null
                && product.getStore() != null
                && product.getStore().getOwner() != null
                && current.getUserId().equals(product.getStore().getOwner().getUserId());
    }

    private boolean ownsOffer(User current, Offer offer) {
        return offer != null && offer.getProduct() != null && ownsProduct(current, offer.getProduct());
    }

    @PostMapping("/product/{productId}")
    public ResponseEntity<Offer> addOffer(@PathVariable Long productId, @RequestBody Offer offer, HttpServletRequest request) {
        User current = requireCurrentUser(request);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));
        if (current.getRole() == User.Role.USER) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        if (current.getRole() == User.Role.STORE_OWNER && !ownsProduct(current, product)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(offerService.addOffer(productId, offer));
    }

    @PatchMapping("/{offerId}")
    public ResponseEntity<Offer> updateOffer(@PathVariable Long offerId, @RequestBody Offer offer, HttpServletRequest request) {
        User current = requireCurrentUser(request);
        Offer existing = offerRepository.findById(offerId)
                .orElseThrow(() -> new RuntimeException("Offer not found with ID: " + offerId));
        if (current.getRole() == User.Role.USER) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        if (current.getRole() == User.Role.STORE_OWNER && !ownsOffer(current, existing)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(offerService.updateOffer(offerId, offer));
    }

    @DeleteMapping("/{offerId}")
    public ResponseEntity<String> deleteOffer(@PathVariable Long offerId, HttpServletRequest request) {
        User current = requireCurrentUser(request);
        Offer existing = offerRepository.findById(offerId)
                .orElseThrow(() -> new RuntimeException("Offer not found with ID: " + offerId));
        if (current.getRole() == User.Role.USER) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        if (current.getRole() == User.Role.STORE_OWNER && !ownsOffer(current, existing)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        offerService.deleteOffer(offerId);
        return ResponseEntity.ok("Offer deleted successfully");
    }

    @PutMapping("/{offerId}/approve")
    public ResponseEntity<String> approveOffer(
            @PathVariable Long offerId,
            @RequestParam boolean approved,
            HttpServletRequest request) {
        User current = requireCurrentUser(request);
        if (current.getRole() != User.Role.ADMIN) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        offerService.setOfferApproval(offerId, approved);
        return ResponseEntity.ok(approved ? "Offer approved" : "Offer rejected");
    }

    @GetMapping
    public ResponseEntity<List<Offer>> getAllOffers() {
        return ResponseEntity.ok(offerService.getAllOffers());
    }

    @GetMapping("/{offerId}")
    public ResponseEntity<Offer> getOfferById(@PathVariable Long offerId) {
        return ResponseEntity.ok(offerService.getOfferById(offerId));
    }

    @GetMapping("/product/{productId}/active")
    public ResponseEntity<List<Offer>> getActiveOffersByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(offerService.getActiveOffersByProduct(productId));
    }

    @GetMapping("/store/{storeId}")
    public ResponseEntity<List<Offer>> getOffersByStore(@PathVariable Long storeId) {
        return ResponseEntity.ok(offerService.getOffersByStore(storeId));
    }

    @GetMapping("/recent")
    public ResponseEntity<List<Offer>> getRecentOffers() {
        return ResponseEntity.ok(offerService.getRecentOffers());
    }

    @GetMapping("/filter")
    public ResponseEntity<List<Offer>> filterOffersByDiscount(
            @RequestParam int min,
            @RequestParam int max) {
        return ResponseEntity.ok(offerService.filterOffersByDiscount(min, max));
    }

    @GetMapping("/{offerId}/status")
    public ResponseEntity<Boolean> isOfferActive(@PathVariable Long offerId) {
        return ResponseEntity.ok(offerService.isOfferActive(offerId));
    }
}
