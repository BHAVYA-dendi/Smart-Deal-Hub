package com.smartdealhub.smartdealhub.service;

import com.smartdealhub.smartdealhub.model.DealInvite;
import com.smartdealhub.smartdealhub.model.InviteStatus;
import com.smartdealhub.smartdealhub.model.GroupDeal;
import com.smartdealhub.smartdealhub.model.User;
import com.smartdealhub.smartdealhub.repository.DealInviteRepository;
import com.smartdealhub.smartdealhub.repository.GroupDealRepository;
import com.smartdealhub.smartdealhub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DealInviteService {
    @Autowired
    private final DealInviteRepository inviteRepository;
    @Autowired
    private final GroupDealRepository groupDealRepository;
    @Autowired
    private final UserRepository userRepository;

    // ================== Send Invite ==================
    public DealInvite sendInvite(Long dealId, Long senderId, Long receiverId) {
        GroupDeal deal = groupDealRepository.findById(dealId)
                .orElseThrow(() -> new RuntimeException("Deal not found"));

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        DealInvite invite = new DealInvite(deal, sender, receiver);
        invite.setStatus(InviteStatus.PENDING);

        return inviteRepository.save(invite);
    }

    // ================== Update Invite Status ==================
    public Optional<DealInvite> updateStatus(Long inviteId, InviteStatus status) {
        return updateStatus(inviteId, status, null, false);
    }

    public Optional<DealInvite> updateStatus(Long inviteId, InviteStatus status, Long currentUserId, boolean isAdmin) {
        Optional<DealInvite> inviteOpt = inviteRepository.findById(inviteId);
        if (inviteOpt.isEmpty()) {
            return inviteOpt;
        }
        DealInvite invite = inviteOpt.get();
        if (!isAdmin) {
            if (currentUserId == null || invite.getReceiver() == null
                    || !currentUserId.equals(invite.getReceiver().getUserId())) {
                throw new RuntimeException("Forbidden");
            }
        }
        invite.setStatus(status);
        inviteRepository.save(invite);
        return Optional.of(invite);
    }

    // ================== Get Invites by Receiver ==================
    public List<DealInvite> getInvitesByReceiver(Long receiverId) {
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));
        return inviteRepository.findByReceiver(receiver);
    }

    // ================== Get Invites by Group Deal ==================
    public List<DealInvite> getInvitesByGroupDeal(Long dealId) {
        GroupDeal deal = groupDealRepository.findById(dealId)
                .orElseThrow(() -> new RuntimeException("Deal not found"));
        return inviteRepository.findByDeal(deal);
    }
}