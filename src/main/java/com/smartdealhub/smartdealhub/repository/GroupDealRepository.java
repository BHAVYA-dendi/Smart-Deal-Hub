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
}