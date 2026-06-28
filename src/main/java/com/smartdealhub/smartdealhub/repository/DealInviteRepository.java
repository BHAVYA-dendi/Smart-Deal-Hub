package com.smartdealhub.smartdealhub.repository;

import com.smartdealhub.smartdealhub.model.DealInvite;
import com.smartdealhub.smartdealhub.model.User;
import com.smartdealhub.smartdealhub.model.GroupDeal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DealInviteRepository extends JpaRepository<DealInvite, Long> {

    // Find all invites by the receiver entity
    List<DealInvite> findByReceiver(User receiver);

    // Find all invites by the sender entity
    List<DealInvite> findBySender(User sender);

    // Find all invites by the associated deal
    List<DealInvite> findByDeal(GroupDeal deal);

    // Find invites by receiver's primary key (adjust field name according to User entity)
    List<DealInvite> findByReceiverUserId(Long receiverId);

    // Find invites by sender's primary key (adjust field name according to User entity)
    List<DealInvite> findBySenderUserId(Long senderId);

    // Find invites by deal's primary key
    List<DealInvite> findByDealId(Long dealId);
}