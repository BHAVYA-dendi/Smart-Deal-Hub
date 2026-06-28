package com.smartdealhub.smartdealhub.repository;

import com.smartdealhub.smartdealhub.model.Offer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OfferRepository extends JpaRepository<Offer, Long> {

    // Find offers by product ID
    List<Offer> findByProductId(Long productId);

    // Find offers by store ID through product
    @Query("SELECT o FROM Offer o WHERE o.product.store.storeId = :storeId")
    List<Offer> findByProductStoreId(Long storeId);

    // Find all approved offers
    List<Offer> findByApprovedTrue();

    // Find active offers by product
    @Query("SELECT o FROM Offer o WHERE o.product.id = :productId AND o.approved = true AND o.startDate <= :now AND o.endDate >= :now")
    List<Offer> findActiveByProduct(Long productId, LocalDateTime now);
}