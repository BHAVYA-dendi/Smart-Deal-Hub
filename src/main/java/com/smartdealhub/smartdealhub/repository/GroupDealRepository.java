package com.smartdealhub.smartdealhub.repository;

import com.smartdealhub.smartdealhub.model.GroupDeal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupDealRepository extends JpaRepository<GroupDeal, Long> {
    List<GroupDeal> findByInitiator_UserId(Long userId);

    // Find deals where a user is a member
    List<GroupDeal> findByMembersUserUserId(Long userId);

    // Find deals for a specific product
    List<GroupDeal> findByProductId(Long productId);
    
    // Find deal by ID with members and user details
    @Query("SELECT DISTINCT gd FROM GroupDeal gd " +
           "LEFT JOIN FETCH gd.members m " +
           "LEFT JOIN FETCH m.user " +
           "WHERE gd.id = :dealId")
    Optional<GroupDeal> findByIdWithMembers(@Param("dealId") Long dealId);

    // Find deals by initiator with members and product to avoid N+1
    @Query("SELECT DISTINCT gd FROM GroupDeal gd " +
           "LEFT JOIN FETCH gd.members m " +
           "LEFT JOIN FETCH gd.product p " +
           "LEFT JOIN FETCH p.store " +
           "WHERE gd.initiator.userId = :userId")
    List<GroupDeal> findByInitiator_UserIdWithDetails(@Param("userId") Long userId);

    // Find deals by user membership with product and store to avoid N+1
    @Query("SELECT DISTINCT gd FROM GroupDeal gd " +
           "LEFT JOIN FETCH gd.members m " +
           "LEFT JOIN FETCH gd.product p " +
           "LEFT JOIN FETCH p.store " +
           "LEFT JOIN FETCH m.user u " +
           "WHERE u.userId = :userId")
    List<GroupDeal> findByMembersUserUserIdWithDetails(@Param("userId") Long userId);

    // Find deals by product with members to avoid N+1
    @Query("SELECT DISTINCT gd FROM GroupDeal gd " +
           "LEFT JOIN FETCH gd.members m " +
           "LEFT JOIN FETCH m.user " +
           "LEFT JOIN FETCH gd.product p " +
           "WHERE p.id = :productId")
    List<GroupDeal> findByProductIdWithMembers(@Param("productId") Long productId);
}