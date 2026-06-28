package com.smartdealhub.smartdealhub.controller;

import com.smartdealhub.smartdealhub.model.Offer;
import com.smartdealhub.smartdealhub.model.Product;
import com.smartdealhub.smartdealhub.service.OfferService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/offers")
@RequiredArgsConstructor
public class OfferController {

    private final OfferService offerService;

    // ==============================
    // 🔹 CREATE / UPDATE / DELETE OFFERS
    // ==============================

    // Add new offer for a product (Store Owner)
    @PostMapping("/product/{productId}")
    public ResponseEntity<Offer> addOffer(@PathVariable Long productId, @RequestBody Offer offer) {
        return ResponseEntity.ok(offerService.addOffer(productId, offer));
    }

    // Update an existing offer
    @PatchMapping("/{offerId}")
    public ResponseEntity<Offer> updateOffer(@PathVariable Long offerId, @RequestBody Offer offer) {
        return ResponseEntity.ok(offerService.updateOffer(offerId, offer));
    }

    // Delete an offer (Store Owner/Admin)
    @DeleteMapping("/{offerId}")
    public ResponseEntity<String> deleteOffer(@PathVariable Long offerId) {
        offerService.deleteOffer(offerId);
        return ResponseEntity.ok("Offer deleted successfully");
    }

    // Approve or reject an offer (Admin or creator)
    @PutMapping("/{offerId}/approve")
    public ResponseEntity<String> approveOffer(
            @PathVariable Long offerId,
            @RequestParam boolean approved) {
        offerService.setOfferApproval(offerId, approved);
        return ResponseEntity.ok(approved ? "Offer approved" : "Offer rejected");
    }

    // ==============================
    // 🔹 VIEW OFFERS (ALL USERS)
    // ==============================

    // Get all offers
    @GetMapping
    public ResponseEntity<List<Offer>> getAllOffers() {
        return ResponseEntity.ok(offerService.getAllOffers());
    }

    // Get offer by ID
    @GetMapping("/{offerId}")
    public ResponseEntity<Offer> getOfferById(@PathVariable Long offerId) {
        return ResponseEntity.ok(offerService.getOfferById(offerId));
    }

    // Get active offers for a product
    @GetMapping("/product/{productId}/active")
    public ResponseEntity<List<Offer>> getActiveOffersByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(offerService.getActiveOffersByProduct(productId));
    }

    // Get offers by store
    @GetMapping("/store/{storeId}")
    public ResponseEntity<List<Offer>> getOffersByStore(@PathVariable Long storeId) {
        return ResponseEntity.ok(offerService.getOffersByStore(storeId));
    }

    // Get recently created offers
    @GetMapping("/recent")
    public ResponseEntity<List<Offer>> getRecentOffers() {
        return ResponseEntity.ok(offerService.getRecentOffers());
    }

    // ==============================
    // 🔹 ADVANCED FILTERS
    // ==============================

    // Filter offers by discount range
    @GetMapping("/filter")
    public ResponseEntity<List<Offer>> filterOffersByDiscount(
            @RequestParam int min,
            @RequestParam int max) {
        return ResponseEntity.ok(offerService.filterOffersByDiscount(min, max));
    }

    // Check if an offer is active
    @GetMapping("/{offerId}/status")
    public ResponseEntity<Boolean> isOfferActive(@PathVariable Long offerId) {
        return ResponseEntity.ok(offerService.isOfferActive(offerId));
    }
}