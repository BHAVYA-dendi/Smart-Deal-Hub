package com.smartdealhub.smartdealhub.service;

import com.smartdealhub.smartdealhub.model.Offer;
import com.smartdealhub.smartdealhub.model.Product;
import com.smartdealhub.smartdealhub.repository.OfferRepository;
import com.smartdealhub.smartdealhub.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OfferService {

    private final OfferRepository offerRepository;
    private final ProductRepository productRepository;

    // ================= CREATE / UPDATE / DELETE =================

    public Offer addOffer(Long productId, Offer offer) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));
        offer.setProduct(product);
        if (offer.getStartDate() == null) offer.setStartDate(LocalDateTime.now());
        if (offer.getEndDate() == null) offer.setEndDate(LocalDateTime.now().plusDays(7));
        offer.setApproved(false); // new offers default to pending approval
        return offerRepository.save(offer);
    }

    public Offer updateOffer(Long offerId, Offer updatedOffer) {
        Offer existing = offerRepository.findById(offerId)
                .orElseThrow(() -> new RuntimeException("Offer not found with ID: " + offerId));
        existing.setTitle(updatedOffer.getTitle());
        existing.setDescription(updatedOffer.getDescription());
        existing.setDiscountPercent(updatedOffer.getDiscountPercent());
        existing.setStartDate(updatedOffer.getStartDate());
        existing.setEndDate(updatedOffer.getEndDate());
        return offerRepository.save(existing);
    }

    public void deleteOffer(Long offerId) {
        offerRepository.deleteById(offerId);
    }

    public void setOfferApproval(Long offerId, boolean approved) {
        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new RuntimeException("Offer not found with ID: " + offerId));
        offer.setApproved(approved);
        offerRepository.save(offer);
    }

    // ================= VIEW OFFERS =================

    public List<Offer> getAllOffers() {
        return offerRepository.findAll().stream()
                .map(this::trimOffer)
                .collect(Collectors.toList());
    }

    public Offer getOfferById(Long offerId) {
        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new RuntimeException("Offer not found with ID: " + offerId));
        return trimOffer(offer);
    }

    public List<Offer> getActiveOffersByProduct(Long productId) {
        return offerRepository.findByProductId(productId).stream()
                .filter(o -> o.getApproved() && o.getStartDate().isBefore(LocalDateTime.now()) && o.getEndDate().isAfter(LocalDateTime.now()))
                .map(this::trimOffer)
                .collect(Collectors.toList());
    }

    public List<Offer> getOffersByStore(Long storeId) {
        return offerRepository.findAll().stream()
                .filter(o -> o.getProduct()!=null && o.getProduct().getStore()!=null && o.getProduct().getStore().getStoreId().equals(storeId))
                .map(this::trimOffer)
                .collect(Collectors.toList());
    }

    public List<Offer> getRecentOffers() {
        return offerRepository.findAll().stream()
                .filter(o -> Boolean.TRUE.equals(o.getApproved()))
                .sorted((a,b) -> {
                    LocalDateTime sa = a.getStartDate();
                    LocalDateTime sb = b.getStartDate();
                    if(sa==null && sb==null) return 0;
                    if(sa==null) return 1;
                    if(sb==null) return -1;
                    return sb.compareTo(sa);
                })
                .map(this::trimOffer)
                .collect(Collectors.toList());
    }

    // ================= ADVANCED FILTERS =================

    public List<Offer> filterOffersByDiscount(int min, int max) {
        return offerRepository.findAll().stream()
                .filter(o -> o.getDiscountPercent() >= min && o.getDiscountPercent() <= max)
                .map(this::trimOffer)
                .collect(Collectors.toList());
    }

    public boolean isOfferActive(Long offerId) {
        Offer offer = getOfferById(offerId);
        LocalDateTime now = LocalDateTime.now();
        return offer.getApproved() && now.isAfter(offer.getStartDate()) && now.isBefore(offer.getEndDate());
    }

    // ================= INTERNAL: TRIM NESTED =================
    private Offer trimOffer(Offer o){
        if(o == null) return null;
        try{
            Product p = o.getProduct();
            if(p != null){
                // break recursive links in nested store/owner
                if(p.getStore()!=null){
                    if(p.getStore().getOwner()!=null){
                        p.getStore().getOwner().setStores(null);
                        p.getStore().getOwner().setPassword(null);
                        p.getStore().getOwner().setFavorites(null);
                        p.getStore().getOwner().setGroupDealsInitiated(null);
                        p.getStore().getOwner().setGroupMemberships(null);
                        p.getStore().getOwner().setNotifications(null);
                        p.getStore().getOwner().setFeedbacks(null);
                        p.getStore().getOwner().setVisitedStores(null);
                        try { p.getStore().getOwner().setReviews(null); } catch(Exception ignored){}
                        try { p.getStore().getOwner().setSentInvites(null); } catch(Exception ignored){}
                        try { p.getStore().getOwner().setReceivedInvites(null); } catch(Exception ignored){}
                    }
                    // remove heavy backrefs on store (only methods that exist)
                    try { p.getStore().setProducts(null); } catch(Exception ignored){}
                    try { p.getStore().setStoreHours(null); } catch(Exception ignored){}
                    try { p.getStore().setFavorites(null); } catch(Exception ignored){}
                    try { p.getStore().setVisitedStores(null); } catch(Exception ignored){}
                }
                // remove heavy collections on product
                try { p.setOffers(null); } catch(Exception ignored){}
                try { p.setReviews(null); } catch(Exception ignored){}
                try { p.setPriceHistoryList(null); } catch(Exception ignored){}
                try { p.setGroupDeals(null); } catch(Exception ignored){}
            }
        }catch(Exception ignored){}
        return o;
    }
}