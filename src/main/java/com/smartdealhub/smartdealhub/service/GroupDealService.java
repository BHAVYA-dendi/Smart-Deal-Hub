package com.smartdealhub.smartdealhub.service;

import com.smartdealhub.smartdealhub.model.*;
import com.smartdealhub.smartdealhub.model.GroupMemberKey;
import com.smartdealhub.smartdealhub.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class GroupDealService {

    private final GroupDealRepository groupDealRepository;
    private final DealInviteRepository dealInviteRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public GroupDealService(GroupDealRepository groupDealRepository,
                            DealInviteRepository dealInviteRepository,
                            GroupMemberRepository groupMemberRepository,
                            UserRepository userRepository,
                            ProductRepository productRepository) {
        this.groupDealRepository = groupDealRepository;
        this.dealInviteRepository = dealInviteRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    // ==============================
    // CREATE / UPDATE / DELETE DEALS
    // ==============================

    public GroupDeal createGroupDeal(Map<String, Object> body) {
        if (body == null) throw new RuntimeException("Invalid deal payload");
        try {
            Long productId = Long.parseLong(String.valueOf(body.get("productId")));
            Long initiatorId = Long.parseLong(String.valueOf(body.get("initiatorId")));
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            if (Boolean.FALSE.equals(product.getGroupDealAllowed())) {
                throw new RuntimeException("Group deals are not allowed for this product");
            }
            User initiator = userRepository.findById(initiatorId)
                    .orElseThrow(() -> new RuntimeException("Initiator not found"));

            GroupDeal deal = new GroupDeal();
            deal.setProduct(product);
            deal.setInitiator(initiator);
            deal.setTitle(product.getDealTitle() != null ? product.getDealTitle() : product.getName());
            deal.setDescription(product.getDealDescription() != null ? product.getDealDescription() : product.getDescription());
            deal.setDiscountPercent(product.getDealDiscountPercent());
            deal.setMaxMembers(product.getDealMaxMembers());
            deal.setDealPrice(product.getDealPrice());
            deal.setCreatedAt(LocalDateTime.now());
            deal.setStatus("PENDING");

            deal = groupDealRepository.save(deal);

            if (groupMemberRepository.findByDeal_IdAndUser_UserId(deal.getId(), initiatorId).isEmpty()) {
                GroupMember creatorMember = new GroupMember(deal, initiator);
                creatorMember.setApproved(true);
                creatorMember.setJoinedAt(LocalDateTime.now());
                deal.addMember(creatorMember);
                groupDealRepository.save(deal);
            }

            return groupDealRepository.findById(deal.getId())
                    .orElseThrow(() -> new RuntimeException("Failed to reload created deal"));
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create group: " + e.getMessage());
        }
    }

    public GroupDeal updateGroupDeal(Long dealId, GroupDeal updatedDeal) {
        GroupDeal deal = groupDealRepository.findById(dealId)
                .orElseThrow(() -> new RuntimeException("Group deal not found with ID: " + dealId));

        deal.setTitle(updatedDeal.getTitle());
        deal.setDescription(updatedDeal.getDescription());
        deal.setDiscountPercent(updatedDeal.getDiscountPercent());
        deal.setMaxMembers(updatedDeal.getMaxMembers());
        return groupDealRepository.save(deal);
    }

    public boolean deleteGroupDeal(Long dealId) {
        Optional<GroupDeal> dealOpt = groupDealRepository.findById(dealId);
        if (dealOpt.isEmpty()) return false;
        groupDealRepository.delete(dealOpt.get());
        return true;
    }

    // ==============================
    // DEAL MEMBERS
    // ==============================

    public GroupMember joinGroupDeal(Long dealId, Long userId) {
        GroupDeal deal = groupDealRepository.findById(dealId)
                .orElseThrow(() -> new RuntimeException("Deal not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Optional<GroupMember> existing = groupMemberRepository.findByDeal_IdAndUser_UserId(dealId, userId);
        if (existing.isPresent()) {
            GroupMember member = existing.get();
            if (member.isApproved()) {
                throw new RuntimeException("User already joined this deal");
            }
            return member;
        }

        Integer cap = deal.getMaxMembers();
        if (cap == null && deal.getProduct() != null) {
            cap = deal.getProduct().getDealMaxMembers();
        }
        long memberCount = groupMemberRepository.findByDeal_Id(dealId).size();
        if (cap != null && memberCount >= cap) {
            throw new RuntimeException("Group deal is full");
        }

        GroupMember member = new GroupMember(deal, user);
        member.setApproved(false);
        member.setJoinedAt(LocalDateTime.now());
        deal.addMember(member);
        groupDealRepository.save(deal);
        return member;
    }

    public boolean leaveGroupDeal(Long dealId, Long userId) {
        GroupMember member = groupMemberRepository.findByDeal_IdAndUser_UserId(dealId, userId)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        GroupDeal deal = member.getDeal();
        if (deal != null) {
            deal.removeMember(member);
        }
        groupMemberRepository.delete(member);
        return true;
    }

    @Transactional(readOnly = true)
    public List<User> getGroupDealMembers(Long dealId) {
        GroupDeal deal = groupDealRepository.findById(dealId)
                .orElseThrow(() -> new RuntimeException("Deal not found"));
        return deal.getMembers().stream()
                .map(GroupMember::getUser)
                .collect(Collectors.toList());
    }

    // ==============================
    // DEAL INVITES
    // ==============================

    public DealInvite sendInvite(Long dealId, Long senderId, Long receiverId) {
        GroupDeal deal = groupDealRepository.findById(dealId)
                .orElseThrow(() -> new RuntimeException("Deal not found"));
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        DealInvite invite = new DealInvite();
        invite.setDeal(deal);
        invite.setSender(sender);
        invite.setReceiver(receiver);
        invite.setStatus(InviteStatus.PENDING);
        invite.setSentAt(LocalDateTime.now());
        return dealInviteRepository.save(invite);
    }

    public void respondToInvite(Long inviteId, boolean approve) {
        DealInvite invite = dealInviteRepository.findById(inviteId)
                .orElseThrow(() -> new RuntimeException("Invite not found"));
        invite.setStatus(approve ? InviteStatus.APPROVED : InviteStatus.REJECTED);
        dealInviteRepository.save(invite);
        if (approve) joinGroupDeal(invite.getDeal().getId(), invite.getReceiver().getUserId());
    }

    @Transactional(readOnly = true)
    public List<DealInvite> getUserInvites(Long userId) {
        return dealInviteRepository.findByReceiverUserId(userId);
    }

    // ==============================
    // VIEW DEALS
    // ==============================

    @Transactional(readOnly = true)
    public List<GroupDeal> getAllDeals() {
        return groupDealRepository.findAll();
    }

    @Transactional(readOnly = true)
    public GroupDeal getDealById(Long dealId) {
        return groupDealRepository.findById(dealId)
                .orElseThrow(() -> new RuntimeException("Deal not found"));
    }

    @Transactional(readOnly = true)
    public double calculateSavings(Long dealId) {
        GroupDeal deal = groupDealRepository.findById(dealId)
                .orElseThrow(() -> new RuntimeException("Deal not found"));
        return deal.getMembers().stream().mapToDouble(m -> m.getUser().getSavings()).sum();
    }

    @Transactional(readOnly = true)
    public List<GroupDeal> getDealsByUser(Long userId) {
        return groupDealRepository.findByInitiator_UserIdWithDetails(userId);
    }

    @Transactional(readOnly = true)
    public List<GroupDeal> getDealsJoinedByUser(Long userId) {
        return groupDealRepository.findByMembersUserUserIdWithDetails(userId);
    }

    @Transactional(readOnly = true)
    public List<GroupDeal> getActiveDealsByProduct(Long productId) {
        return groupDealRepository.findByProductIdWithMembers(productId).stream()
                .filter(deal -> {
                    Integer cap = deal.getProduct() != null ? deal.getProduct().getDealMaxMembers() : null;
                    return cap == null || deal.getMembers().size() < cap;
                })
                .collect(Collectors.toList());
    }

    // ==============================
    // APPROVE / REJECT MEMBERS
    // ==============================

    public GroupMember approveMember(Long dealId, Long memberId) {
        GroupMember member = groupMemberRepository.findByDeal_IdAndUser_UserId(dealId, memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        member.setApproved(true);
        return groupMemberRepository.save(member);
    }

    public GroupMember rejectMember(Long dealId, Long memberId) {
        GroupMember member = groupMemberRepository.findByDeal_IdAndUser_UserId(dealId, memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        GroupDeal deal = member.getDeal();
        if (deal != null) {
            deal.removeMember(member);
        }
        groupMemberRepository.delete(member);
        return member;
    }

    @Transactional(readOnly = true)
    public List<User> getPendingMembers(Long dealId) {
        return groupMemberRepository.findByDeal_IdAndApprovedFalse(dealId)
                .stream()
                .map(GroupMember::getUser)
                .collect(Collectors.toList());
    }
}
