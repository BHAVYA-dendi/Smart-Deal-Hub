package com.smartdealhub.smartdealhub.controller;

import com.smartdealhub.smartdealhub.model.*;
import com.smartdealhub.smartdealhub.repository.GroupDealRepository;
import com.smartdealhub.smartdealhub.repository.GroupMemberRepository;
import jakarta.servlet.http.HttpServletRequest;
import com.smartdealhub.smartdealhub.service.GroupDealService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * GroupDealController - Manages group deals and group_member table operations
 * 
 * group_member table structure:
 * - deal_id (bigint PK) - References group_deal.id
 * - user_id (bigint PK) - References users.user_id  
 * - joined_at (datetime) - When user joined/requested to join
 * - approved (tinyint(1)) - 0 = pending approval, 1 = approved
 * 
 * Operations:
 * - Create deal: Inserts creator into group_member with approved=1
 * - Join deal: Inserts user into group_member with approved=0 (pending)
 * - Approve member: Updates group_member SET approved=1
 * - Reject member: Deletes from group_member table
 * - Leave deal: Deletes from group_member table
 * - Get pending: Queries group_member WHERE approved=0
 * - Get user deals: Queries group_member JOIN group_deal WHERE user_id=X AND approved=1
 * 
 * Required Service Methods (implement these in GroupDealService):
 * - getDealsJoinedByUser(userId): SELECT gd.* FROM group_member gm JOIN group_deal gd ON gm.deal_id=gd.id WHERE gm.user_id=userId AND gm.approved=1
 * - getDealCreator(dealId): SELECT MIN(user_id) FROM group_member WHERE deal_id=dealId AND approved=1
 * - approveMember(dealId, userId): UPDATE group_member SET approved=1 WHERE deal_id=dealId AND user_id=userId
 * - rejectMember(dealId, userId): DELETE FROM group_member WHERE deal_id=dealId AND user_id=userId
 * - getPendingMembers(dealId): SELECT u.* FROM group_member gm JOIN users u ON gm.user_id=u.user_id WHERE gm.deal_id=dealId AND gm.approved=0
 */
@RestController
@RequestMapping("/api/group-deals")
@CrossOrigin(origins = "*")
@SuppressWarnings("unused")
public class GroupDealController {

    private final GroupDealService groupDealService;
    private final GroupDealRepository groupDealRepository;
    private final GroupMemberRepository groupMemberRepository;

    public GroupDealController(GroupDealService groupDealService,
                               GroupDealRepository groupDealRepository,
                               GroupMemberRepository groupMemberRepository) {
        this.groupDealService = groupDealService;
        this.groupDealRepository = groupDealRepository;
        this.groupMemberRepository = groupMemberRepository;
    }

    private User requireCurrentUser(HttpServletRequest request) {
        Object cu = request.getAttribute("currentUser");
        if (cu instanceof User user) return user;
        throw new RuntimeException("Unauthenticated");
    }

    private boolean canAccessUser(User current, Long userId) {
        return current.getRole() == User.Role.ADMIN || current.getUserId().equals(userId);
    }

    private boolean canModerateDeal(User current, GroupDeal deal) {
        if (current.getRole() == User.Role.ADMIN) return true;
        return deal.getInitiator() != null && current.getUserId().equals(deal.getInitiator().getUserId());
    }

    private Map<String, Object> buildDealDto(GroupDeal deal, Long viewerUserId) {
        long totalMembers = groupMemberRepository.countByDeal_Id(deal.getId());
        long approvedMembers = groupMemberRepository.countByDeal_IdAndApprovedTrue(deal.getId());
        long pendingMembers = groupMemberRepository.countByDeal_IdAndApprovedFalse(deal.getId());

        boolean isCreator = deal.getInitiator() != null && deal.getInitiator().getUserId() != null
                && viewerUserId != null && deal.getInitiator().getUserId().equals(viewerUserId);
        Long creatorId = deal.getInitiator() != null ? deal.getInitiator().getUserId() : null;

        Integer maxMembers = deal.getMaxMembers();
        if (maxMembers == null && deal.getProduct() != null) {
            maxMembers = deal.getProduct().getDealMaxMembers();
        }

        String membershipStatus = "NONE";
        if (viewerUserId != null) {
            Optional<GroupMember> membership = groupMemberRepository.findByDeal_IdAndUser_UserId(deal.getId(), viewerUserId);
            if (membership.isPresent()) {
                membershipStatus = membership.get().isApproved() ? "APPROVED" : "PENDING";
            }
        }

        Map<String, Object> m = new HashMap<>();
        m.put("id", deal.getId());
        m.put("productId", deal.getProduct() != null ? deal.getProduct().getId() : null);
        m.put("productName", deal.getProduct() != null ? deal.getProduct().getName() : null);
        m.put("memberCount", (int) totalMembers);
        m.put("currentMembers", (int) approvedMembers);
        m.put("pendingMembers", (int) pendingMembers);
        m.put("maxMembers", maxMembers != null ? maxMembers : 10);
        m.put("status", deal.getStatus());
        m.put("createdAt", deal.getCreatedAt());
        m.put("creatorId", creatorId);
        m.put("isCreator", isCreator);
        m.put("isMember", !"NONE".equals(membershipStatus));
        m.put("membershipStatus", membershipStatus);
        m.put("userRole", isCreator ? "CREATOR" : ("APPROVED".equals(membershipStatus) || "PENDING".equals(membershipStatus) ? "MEMBER" : "NONE"));
        if (deal.getDealPrice() != null) {
            m.put("dealPrice", deal.getDealPrice());
        }
        if (deal.getDiscountPercent() != null) {
            m.put("dealDiscount", deal.getDiscountPercent());
        }
        return m;
    }

    private void putMemberCounts(Map<String, Object> m, Long dealId) {
        m.put("memberCount", (int) groupMemberRepository.countByDeal_Id(dealId));
        m.put("currentMembers", (int) groupMemberRepository.countByDeal_IdAndApprovedTrue(dealId));
        m.put("pendingMembers", (int) groupMemberRepository.countByDeal_IdAndApprovedFalse(dealId));
    }

    private Map<String, Object> buildJoinableDealDto(GroupDeal deal) {
        long totalMembers = groupMemberRepository.countByDeal_Id(deal.getId());
        long approvedMembers = groupMemberRepository.countByDeal_IdAndApprovedTrue(deal.getId());

        Map<String, Object> dealInfo = new HashMap<>();
        dealInfo.put("id", deal.getId());
        dealInfo.put("title", deal.getTitle());
        dealInfo.put("description", deal.getDescription());
        dealInfo.put("productName", deal.getProduct() != null ? deal.getProduct().getName() : null);
        dealInfo.put("memberCount", (int) totalMembers);
        dealInfo.put("currentMembers", (int) approvedMembers);
        dealInfo.put("maxMembers", deal.getMaxMembers() != null ? deal.getMaxMembers() : 10);
        dealInfo.put("createdAt", deal.getCreatedAt());
        dealInfo.put("creatorName", deal.getInitiator() != null ? deal.getInitiator().getName() : "Unknown");
        return dealInfo;
    }
    
    // Remove the second constructor to avoid initialization issues
    // public GroupDealController(GroupDealService groupDealService) {
    //     this.groupDealService = groupDealService;
    //     this.groupDealRepository = null; // This would cause NPE
    // }

    // Create a deal (user-only minimal payload) — return lightweight summary to avoid deep recursion
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createDeal(@RequestBody Map<String,Object> body, HttpServletRequest request) {
        try {
            User current = requireCurrentUser(request);
            body.put("initiatorId", current.getUserId());
            GroupDeal deal = groupDealService.createGroupDeal(body);

            Map<String, Object> dto = new HashMap<>();
            dto.put("id", deal.getId());
            dto.put("productId", deal.getProduct()!=null? deal.getProduct().getId(): null);
            dto.put("productName", deal.getProduct()!=null? deal.getProduct().getName(): null);
            dto.put("memberCount", (int) groupMemberRepository.countByDeal_Id(deal.getId()));
            dto.put("currentMembers", (int) groupMemberRepository.countByDeal_IdAndApprovedTrue(deal.getId()));
            dto.put("maxMembers", deal.getMaxMembers() != null ? deal.getMaxMembers() : 10);
            dto.put("status", deal.getStatus());
            dto.put("creatorId", current.getUserId());
            dto.put("success", true);
            dto.put("message", "Group deal created successfully");
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // Get all deals — return trimmed summaries to avoid recursive JSON
    @GetMapping("/all")
    public ResponseEntity<List<Map<String, Object>>> getAllDeals() {
        List<GroupDeal> list = groupDealService.getAllDeals();
        List<Map<String, Object>> dto = list.stream().map(deal -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", deal.getId());
            m.put("productId", deal.getProduct() != null ? deal.getProduct().getId() : null);
            m.put("productName", deal.getProduct() != null ? deal.getProduct().getName() : null);
            putMemberCounts(m, deal.getId());
            m.put("maxMembers", deal.getMaxMembers() != null ? deal.getMaxMembers() : 10);
            m.put("status", deal.getStatus());
            m.put("creatorId", deal.getInitiator() != null ? deal.getInitiator().getUserId() : null);
            m.put("createdAt", deal.getCreatedAt());
            return m;
        }).toList();
        return ResponseEntity.ok(dto);
    }

    // Current user's deals (JWT — no userId in path)
    @GetMapping("/mine")
    public ResponseEntity<List<Map<String, Object>>> getMyDeals(HttpServletRequest request) {
        User current = requireCurrentUser(request);
        return getAllDealsForUser(current.getUserId(), request);
    }

    // Joinable deals for current user (JWT)
    @GetMapping("/joinable/me")
    public ResponseEntity<List<Map<String, Object>>> getJoinableDealsForMe(HttpServletRequest request) {
        User current = requireCurrentUser(request);
        return getJoinableDeals(current.getUserId(), request);
    }

    // Get deal by id — return trimmed summary
    @GetMapping("/{id}")
    public ResponseEntity<Map<String,Object>> getDeal(@PathVariable Long id) {
        GroupDeal deal = groupDealService.getDealById(id);
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", deal.getId());
        dto.put("productId", deal.getProduct() != null ? deal.getProduct().getId() : null);
        dto.put("productName", deal.getProduct() != null ? deal.getProduct().getName() : null);
        putMemberCounts(dto, deal.getId());
        dto.put("maxMembers", deal.getMaxMembers() != null ? deal.getMaxMembers() : 10);
        dto.put("status", deal.getStatus());
        return ResponseEntity.ok(dto);
    }

    // Update deal
    @PatchMapping("/update/{id}")
    public ResponseEntity<GroupDeal> updateDeal(@PathVariable Long id, @RequestBody GroupDeal deal, HttpServletRequest request) {
        User current = requireCurrentUser(request);
        GroupDeal existing = groupDealService.getDealById(id);
        if (!canModerateDeal(current, existing)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(groupDealService.updateGroupDeal(id, deal));
    }

    // Delete deal
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteDeal(@PathVariable Long id, HttpServletRequest request) {
        User current = requireCurrentUser(request);
        GroupDeal existing = groupDealService.getDealById(id);
        if (!canModerateDeal(current, existing)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        groupDealService.deleteGroupDeal(id);
        return ResponseEntity.ok("Deal deleted successfully");
    }

    // Get all joinable deals for a user (not created by user and not already joined)
    @GetMapping("/joinable/{userId}")
    public ResponseEntity<List<Map<String, Object>>> getJoinableDeals(@PathVariable Long userId, HttpServletRequest request) {
        try {
            User current = requireCurrentUser(request);
            if (!canAccessUser(current, userId)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            // Get all active deals
            List<GroupDeal> allDeals = groupDealService.getAllDeals();
            
            // Get deals user has already joined
            List<GroupDeal> userDeals = groupDealService.getDealsJoinedByUser(userId);
            Set<Long> userDealIds = userDeals.stream()
                .map(GroupDeal::getId)
                .collect(Collectors.toSet());
            
            // Filter out deals user has already joined or created
            List<Map<String, Object>> joinableDeals = allDeals.stream()
                .filter(deal -> !userDealIds.contains(deal.getId()) &&
                              (deal.getInitiator() == null || !deal.getInitiator().getUserId().equals(userId)))
                .map(this::buildJoinableDealDto)
                .collect(Collectors.toList());

            return ResponseEntity.ok(joinableDeals);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    // Join deal - inserts into group_member table
    @PostMapping("/{dealId}/join")
    public ResponseEntity<Map<String, Object>> joinDeal(
            @PathVariable Long dealId, 
            @RequestParam Long userId,
            HttpServletRequest request) {
        try {
            User current = requireCurrentUser(request);
            if (!canAccessUser(current, userId)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            if (groupMemberRepository.findByDeal_IdAndUser_UserId(dealId, userId).isPresent()) {
                GroupMember existing = groupMemberRepository.findByDeal_IdAndUser_UserId(dealId, userId).get();
                if (existing.isApproved()) {
                    return ResponseEntity.badRequest().body(Map.of(
                            "success", false,
                            "message", "You have already joined this deal"
                    ));
                }
                Map<String, Object> pending = new HashMap<>();
                pending.put("success", true);
                pending.put("message", "Join request already pending approval");
                pending.put("approved", false);
                pending.put("dealId", dealId);
                pending.put("userId", userId);
                pending.put("memberCount", groupMemberRepository.countByDeal_Id(dealId));
                pending.put("currentMembers", groupMemberRepository.countByDeal_IdAndApprovedTrue(dealId));
                return ResponseEntity.ok(pending);
            }

            groupDealService.joinGroupDeal(dealId, userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Successfully requested to join the deal");
            GroupDeal joinedDeal = groupDealService.getDealById(dealId);
            putMemberCounts(response, dealId);
            response.put("maxMembers", joinedDeal.getMaxMembers() != null ? joinedDeal.getMaxMembers() : 10);
            response.put("dealId", dealId);
            response.put("userId", userId);
            response.put("approved", false);

            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Error joining deal: " + e.getMessage()
            ));
        }
    }

    // Leave deal
    @PostMapping("/{dealId}/leave")
    public ResponseEntity<String> leaveDeal(@PathVariable Long dealId, @RequestParam Long userId, HttpServletRequest request) {
        User current = requireCurrentUser(request);
        if (!canAccessUser(current, userId)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        groupDealService.leaveGroupDeal(dealId, userId);
        return ResponseEntity.ok("Left deal successfully");
    }

    // Get members of a deal
    @GetMapping("/{dealId}/members")
    public ResponseEntity<List<User>> getDealMembers(@PathVariable Long dealId) {
        List<User> members = groupDealService.getGroupDealMembers(dealId);
        return ResponseEntity.ok(members);
    }

    // Calculate savings for a deal
    @GetMapping("/{dealId}/savings")
    public ResponseEntity<Double> calculateSavings(@PathVariable Long dealId) {
        return ResponseEntity.ok(groupDealService.calculateSavings(dealId));
    }

    // Get all deals for a specific user from group_members with role info
    @GetMapping("/user/{userId}/all")
    public ResponseEntity<List<Map<String, Object>>> getAllDealsForUser(@PathVariable Long userId, HttpServletRequest request) {
        try {
            User current = requireCurrentUser(request);
            if (!canAccessUser(current, userId)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            // Fetch deals the user joined (group_members)
            List<GroupDeal> joined = new ArrayList<>();
            try { joined = groupDealService.getDealsJoinedByUser(userId); } catch (Exception ignored) {}

            // Fetch deals the user created (initiator)
            List<GroupDeal> created = new ArrayList<>();
            try { created = groupDealService.getDealsByUser(userId); } catch (Exception ignored) {}

            // Union by deal id
            Map<Long, GroupDeal> byId = new java.util.LinkedHashMap<>();
            for (GroupDeal d : joined) { if (d != null && d.getId() != null) byId.putIfAbsent(d.getId(), d); }
            for (GroupDeal d : created) { if (d != null && d.getId() != null) byId.put(d.getId(), d); }

            List<Map<String, Object>> out = new ArrayList<>();
            for (GroupDeal deal : byId.values()) {
                out.add(buildDealDto(deal, userId));
            }

            return ResponseEntity.ok(out);
        } catch (Exception e) {
            return ResponseEntity.ok(new ArrayList<>());
        }
    }
    
    // Helper method to extract user ID from member object using reflection
    private Long extractUserIdFromMember(Object member) {
        if (member == null) return null;
        
        try {
            // Try direct userId field
            java.lang.reflect.Field userIdField = member.getClass().getDeclaredField("userId");
            userIdField.setAccessible(true);
            Object userIdValue = userIdField.get(member);
            if (userIdValue != null) {
                return Long.valueOf(userIdValue.toString());
            }
        } catch (Exception e) {
            // Try user.userId pattern
            try {
                java.lang.reflect.Method getUserMethod = member.getClass().getMethod("getUser");
                Object user = getUserMethod.invoke(member);
                if (user != null) {
                    java.lang.reflect.Method getUserIdMethod = user.getClass().getMethod("getUserId");
                    return (Long) getUserIdMethod.invoke(user);
                }
            } catch (Exception e2) {
                // Ignore reflection errors
            }
        }
        return null;
    }

    // Get deals created by a specific user
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<GroupDeal>> getDealsByUser(@PathVariable Long userId, HttpServletRequest request) {
        User current = requireCurrentUser(request);
        if (!canAccessUser(current, userId)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(groupDealService.getDealsByUser(userId));
    }

    // Get deals joined by a specific user
    @GetMapping("/joined/{userId}")
    public ResponseEntity<List<GroupDeal>> getDealsJoinedByUser(@PathVariable Long userId, HttpServletRequest request) {
        User current = requireCurrentUser(request);
        if (!canAccessUser(current, userId)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(groupDealService.getDealsJoinedByUser(userId));
    }

    // Test endpoint to verify API is working
    @GetMapping("/test/{userId}")
    public ResponseEntity<Map<String, Object>> testUserEndpoint(@PathVariable Long userId, HttpServletRequest request) {
        User current = requireCurrentUser(request);
        if (!canAccessUser(current, userId)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        Map<String, Object> response = new HashMap<>();
        response.put("message", "API is working");
        response.put("userId", userId);
        response.put("timestamp", System.currentTimeMillis());
        
        try {
            List<GroupDeal> allDeals = groupDealService.getAllDeals();
            response.put("totalDeals", allDeals.size());
            response.put("dealsFound", allDeals.size() > 0);
            
            // Debug: Show deal structure
            if (!allDeals.isEmpty()) {
                GroupDeal firstDeal = allDeals.get(0);
                Map<String, Object> dealStructure = new HashMap<>();
                dealStructure.put("id", firstDeal.getId());
                dealStructure.put("hasMembers", firstDeal.getMembers() != null);
                dealStructure.put("memberCount", firstDeal.getMembers() != null ? firstDeal.getMembers().size() : 0);
                
                if (firstDeal.getMembers() != null && !firstDeal.getMembers().isEmpty()) {
                    Object firstMember = firstDeal.getMembers().iterator().next();
                    dealStructure.put("memberClass", firstMember.getClass().getSimpleName());
                    
                    // Try to get member fields
                    try {
                        java.lang.reflect.Field[] fields = firstMember.getClass().getDeclaredFields();
                        List<String> fieldNames = new ArrayList<>();
                        for (java.lang.reflect.Field field : fields) {
                            fieldNames.add(field.getName());
                        }
                        dealStructure.put("memberFields", fieldNames);
                    } catch (Exception e) {
                        dealStructure.put("memberFieldsError", e.getMessage());
                    }
                }
                
                response.put("sampleDeal", dealStructure);
            }
        } catch (Exception e) {
            response.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    // Approve all pending join requests for a deal (creator or admin)
    @PostMapping("/{dealId}/approve-all")
    public ResponseEntity<Map<String, Object>> approveAllPendingMembers(@PathVariable Long dealId, HttpServletRequest request) {
        try {
            User current = requireCurrentUser(request);
            GroupDeal deal = groupDealService.getDealById(dealId);
            if (!canModerateDeal(current, deal)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

            List<User> pendingUsers = groupDealService.getPendingMembers(dealId);
            int approvedCount = 0;
            for (User pendingUser : pendingUsers) {
                groupDealService.approveMember(dealId, pendingUser.getUserId());
                approvedCount++;
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", approvedCount > 0
                    ? "Approved " + approvedCount + " pending member(s)"
                    : "No pending members to approve");
            response.put("dealId", dealId);
            response.put("approvedCount", approvedCount);
            putMemberCounts(response, dealId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Error approving members: " + e.getMessage()
            ));
        }
    }

    // Approve member join request - updates group_member table
    @PostMapping("/{dealId}/approve/{userId}")
    public ResponseEntity<Map<String, Object>> approveMember(@PathVariable Long dealId, @PathVariable Long userId, HttpServletRequest request) {
        try {
            User current = requireCurrentUser(request);
            GroupDeal deal = groupDealService.getDealById(dealId);
            if (!canModerateDeal(current, deal)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            GroupMember approvedMember = groupDealService.approveMember(dealId, userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Member approved successfully");
            response.put("dealId", dealId);
            response.put("userId", userId);
            response.put("approved", true);
            putMemberCounts(response, dealId);
            response.put("userName", approvedMember.getUser().getName());
            response.put("userEmail", approvedMember.getUser().getEmail());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Error approving member: " + e.getMessage()
            ));
        }
    }

    // Reject member join request - removes from group_member table
    @PostMapping("/{dealId}/reject/{userId}")
    public ResponseEntity<Map<String, Object>> rejectMember(@PathVariable Long dealId, @PathVariable Long userId, HttpServletRequest request) {
        try {
            User current = requireCurrentUser(request);
            GroupDeal deal = groupDealService.getDealById(dealId);
            if (!canModerateDeal(current, deal)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            groupDealService.rejectMember(dealId, userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Member rejected and removed from deal");
            response.put("dealId", dealId);
            response.put("userId", userId);
            putMemberCounts(response, dealId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Error rejecting member: " + e.getMessage()
            ));
        }
    }

    // Get pending join requests for a deal (for creator approval)
    @GetMapping("/{dealId}/pending")
    public ResponseEntity<List<Map<String, Object>>> getPendingRequests(@PathVariable Long dealId, HttpServletRequest request) {
        try {
            User current = requireCurrentUser(request);
            GroupDeal deal = groupDealService.getDealById(dealId);
            if (!canModerateDeal(current, deal)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            List<User> pendingUsers = groupDealService.getPendingMembers(dealId);

            List<Map<String, Object>> dto = pendingUsers.stream().map(user -> {
                Map<String, Object> m = new HashMap<>();
                m.put("userId", user.getUserId());
                m.put("name", user.getName());
                m.put("email", user.getEmail());
                m.put("dealId", dealId);
                m.put("approved", false);
                return m;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.ok(new ArrayList<>()); // Return empty list on error
        }
    }
}