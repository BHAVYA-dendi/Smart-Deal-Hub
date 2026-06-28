package com.smartdealhub.smartdealhub.service;

import com.smartdealhub.smartdealhub.model.*;
import com.smartdealhub.smartdealhub.model.GroupMemberKey;
import com.smartdealhub.smartdealhub.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Map;
import java.util.stream.Collectors;

@Service
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

    public GroupDeal createGroupDeal(Map<String,Object> body){
        if(body==null) throw new RuntimeException("Invalid deal payload");
        try{
            Long productId = Long.parseLong(String.valueOf(body.get("productId")));
            Long initiatorId = Long.parseLong(String.valueOf(body.get("initiatorId")));
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            if(Boolean.FALSE.equals(product.getGroupDealAllowed())){
                throw new RuntimeException("Group deals are not allowed for this product");
            }
            User initiator = userRepository.findById(initiatorId)
                    .orElseThrow(() -> new RuntimeException("Initiator not found"));

            GroupDeal deal = new GroupDeal();
            deal.setProduct(product);
            deal.setInitiator(initiator);
            // Populate deal fields from product
            deal.setTitle(product.getDealTitle()!=null ? product.getDealTitle() : product.getName());
            deal.setDescription(product.getDealDescription()!=null ? product.getDealDescription() : product.getDescription());
            deal.setDiscountPercent(product.getDealDiscountPercent());
            deal.setMaxMembers(product.getDealMaxMembers());
            deal.setDealPrice(product.getDealPrice());
            deal.setCreatedAt(LocalDateTime.now());
            deal.setStatus("PENDING");

            // Save deal first to get ID
            deal = groupDealRepository.save(deal);

            // Auto-add creator as approved member into group_members
            GroupMember creatorMember = new GroupMember(deal, initiator);
            creatorMember.setApproved(true);
            creatorMember.setJoinedAt(LocalDateTime.now());
            deal.getMembers().add(creatorMember);

            return groupDealRepository.save(deal);
        }catch(RuntimeException re){ throw re; }
        catch(Exception e){ throw new RuntimeException("Failed to create group: "+e.getMessage()); }
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
    // 🔹 DEAL MEMBERS
    // ==============================

    public GroupMember joinGroupDeal(Long dealId, Long userId) {
        GroupDeal deal = groupDealRepository.findById(dealId)
                .orElseThrow(() -> new RuntimeException("Deal not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Set<GroupMember> members = deal.getMembers();
        boolean alreadyJoined = members.stream().anyMatch(m -> m.getUser().getUserId().equals(userId));
        if (alreadyJoined) throw new RuntimeException("User already joined this deal");
        Integer cap = deal.getProduct()!=null? deal.getProduct().getDealMaxMembers() : null;
        if (cap!=null && members.size() >= cap) throw new RuntimeException("Group deal is full");

        // Create member with composite key and default approved=false
        GroupMember member = new GroupMember(deal, user);
        // joinedAt already defaults to now, approved defaults to false
        members.add(member);
        deal.setMembers(members);
        groupMemberRepository.save(member);
        groupDealRepository.save(deal);
        return member;
    }

    public boolean leaveGroupDeal(Long dealId, Long userId) {
        GroupDeal deal = groupDealRepository.findById(dealId)
                .orElseThrow(() -> new RuntimeException("Deal not found"));
        Set<GroupMember> members = deal.getMembers();
        boolean removed = members.removeIf(m -> m.getUser().getUserId().equals(userId));
        deal.setMembers(members);
        if (removed) {
            groupMemberRepository.deleteById(new GroupMemberKey(dealId, userId));
        }
        groupDealRepository.save(deal);
        return removed;
    }

    public List<User> getGroupDealMembers(Long dealId) {
        GroupDeal deal = groupDealRepository.findById(dealId)
                .orElseThrow(() -> new RuntimeException("Deal not found"));
        return deal.getMembers().stream()
                .map(GroupMember::getUser)
                .collect(Collectors.toList());
    }

    // ==============================
    // 🔹 DEAL INVITES
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

    public List<DealInvite> getUserInvites(Long userId) {
        return dealInviteRepository.findByReceiverUserId(userId);
    }

    // ==============================
    // 🔹 VIEW DEALS
    // ==============================

    public List<GroupDeal> getAllDeals() {
        return groupDealRepository.findAll();
    }

    public GroupDeal getDealById(Long dealId) {
        return groupDealRepository.findById(dealId)
                .orElseThrow(() -> new RuntimeException("Deal not found"));
    }

    public double calculateSavings(Long dealId) {
        GroupDeal deal = groupDealRepository.findById(dealId)
                .orElseThrow(() -> new RuntimeException("Deal not found"));
        return deal.getMembers().stream().mapToDouble(m -> m.getUser().getSavings()).sum();
    }

    public List<GroupDeal> getDealsByUser(Long userId) {
        return groupDealRepository.findByInitiator_UserId(userId);
    }

    public List<GroupDeal> getDealsJoinedByUser(Long userId) {
        return groupDealRepository.findByMembersUserUserId(userId);
    }
    // In GroupDealService.java
    public List<GroupDeal> getActiveDealsByProduct(Long productId) {
        return groupDealRepository.findByProductId(productId).stream()
                .filter(deal -> {
                    Integer cap = deal.getProduct()!=null? deal.getProduct().getDealMaxMembers() : null;
                    return cap==null || deal.getMembers().size() < cap;
                })
                .collect(Collectors.toList());
    }

    // ==============================
    // 🔹 APPROVE / REJECT MEMBERS
    // ==============================

    public GroupMember approveMember(Long dealId, Long memberId) {
        GroupMember member = groupMemberRepository.findByDeal_IdAndUser_UserId(dealId, memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        member.setApproved(true);
        groupMemberRepository.save(member);
        return member;
    }

    public GroupMember rejectMember(Long dealId, Long memberId) {
        GroupMember member = groupMemberRepository.findByDeal_IdAndUser_UserId(dealId, memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        groupMemberRepository.delete(member);
        return member;
    }

    public List<User> getPendingMembers(Long dealId) {
        return groupMemberRepository.findByDeal_IdAndApprovedFalse(dealId)
                .stream()
                .map(GroupMember::getUser)
                .collect(Collectors.toList());
    }
}