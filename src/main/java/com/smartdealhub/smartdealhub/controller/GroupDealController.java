package com.smartdealhub.smartdealhub.controller;

import com.smartdealhub.smartdealhub.model.*;
import com.smartdealhub.smartdealhub.repository.GroupDealRepository;
import com.smartdealhub.smartdealhub.service.GroupDealService;
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

    public GroupDealController(GroupDealService groupDealService, GroupDealRepository groupDealRepository) {
        this.groupDealService = groupDealService;
        this.groupDealRepository = groupDealRepository;
    }
    
    // Remove the second constructor to avoid initialization issues
    // public GroupDealController(GroupDealService groupDealService) {
    //     this.groupDealService = groupDealService;
    //     this.groupDealRepository = null; // This would cause NPE
    // }

    // Create a deal (user-only minimal payload) — return lightweight summary to avoid deep recursion
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createDeal(@RequestBody Map<String,Object> body) {
        try {
            // Ensure the creator is automatically added as a member
            GroupDeal deal = groupDealService.createGroupDeal(body);
            
            // Try to add creator as member if initiatorId is provided
            Object initiatorId = body.get("initiatorId");
            if (initiatorId != null) {
                try {
                    Long userId = Long.valueOf(initiatorId.toString());
                    // Join the deal and set as approved since creator auto-joins
                    Object joinResult = groupDealService.joinGroupDeal(deal.getId(), userId);
                    
                    // Creator is automatically approved when joining
                    // Note: If your service has an approveJoinRequest method, uncomment below:
                    // groupDealService.approveJoinRequest(deal.getId(), userId);
                } catch (Exception e) {
                    // Log the error but don't fail deal creation
                    System.err.println("Failed to add creator as member: " + e.getMessage());
                }
            }
            
            // Refresh deal to get updated member count
            deal = groupDealService.getDealById(deal.getId());
            
            Map<String, Object> dto = new HashMap<>();
            dto.put("id", deal.getId());
            dto.put("productId", deal.getProduct()!=null? deal.getProduct().getId(): null);
            dto.put("productName", deal.getProduct()!=null? deal.getProduct().getName(): null);
            dto.put("memberCount", deal.getMembers()!=null? deal.getMembers().size(): 0);
            dto.put("maxMembers", deal.getMaxMembers() != null ? deal.getMaxMembers() : 10);
            dto.put("status", deal.getStatus());
            // Set creatorId from initiatorId
            dto.put("creatorId", initiatorId);
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
            m.put("memberCount", deal.getMembers() != null ? deal.getMembers().size() : 0);
            m.put("maxMembers", deal.getMaxMembers() != null ? deal.getMaxMembers() : 10);
            m.put("status", deal.getStatus());
            // Set creatorId to null for now - will be properly implemented when creator field is added to GroupDeal
            m.put("creatorId", null);
            m.put("createdAt", deal.getCreatedAt());
            return m;
        }).toList();
        return ResponseEntity.ok(dto);
    }

    // Get deal by id — return trimmed summary
    @GetMapping("/{id}")
    public ResponseEntity<Map<String,Object>> getDeal(@PathVariable Long id) {
        GroupDeal deal = groupDealService.getDealById(id);
        Map<String,Object> dto = Map.of(
                "id", deal.getId(),
                "productId", deal.getProduct()!=null? deal.getProduct().getId(): null,
                "productName", deal.getProduct()!=null? deal.getProduct().getName(): null,
                "memberCount", deal.getMembers()!=null? deal.getMembers().size(): 0,
                "status", deal.getStatus()
        );
        return ResponseEntity.ok(dto);
    }

    // Update deal
    @PatchMapping("/update/{id}")
    public ResponseEntity<GroupDeal> updateDeal(@PathVariable Long id, @RequestBody GroupDeal deal) {
        return ResponseEntity.ok(groupDealService.updateGroupDeal(id, deal));
    }

    // Delete deal
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteDeal(@PathVariable Long id) {
        groupDealService.deleteGroupDeal(id);
        return ResponseEntity.ok("Deal deleted successfully");
    }

    // Get all joinable deals for a user (not created by user and not already joined)
    @GetMapping("/joinable/{userId}")
    public ResponseEntity<List<Map<String, Object>>> getJoinableDeals(@PathVariable Long userId) {
        try {
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
                .map(deal -> {
                    Map<String, Object> dealInfo = new HashMap<>();
                    dealInfo.put("id", deal.getId());
                    dealInfo.put("title", deal.getTitle());
                    dealInfo.put("description", deal.getDescription());
                    dealInfo.put("currentMembers", deal.getMembers() != null ? deal.getMembers().size() : 0);
                    dealInfo.put("maxMembers", deal.getMaxMembers() != null ? deal.getMaxMembers() : 10);
                    dealInfo.put("createdAt", deal.getCreatedAt());
                    
                    // Handle creator name safely
                    String creatorName = "Unknown";
                    if (deal.getInitiator() != null) {
                        // Use getter methods that exist in your User class
                        // Common alternatives: getFirstname()/getLastname() or getName()
                        String name = "";
                        try {
                            // Try common getter patterns
                            if (deal.getInitiator().getClass().getMethod("getFirstname") != null && 
                                deal.getInitiator().getClass().getMethod("getLastname") != null) {
                                String firstName = (String) deal.getInitiator().getClass()
                                    .getMethod("getFirstname").invoke(deal.getInitiator());
                                String lastName = (String) deal.getInitiator().getClass()
                                    .getMethod("getLastname").invoke(deal.getInitiator());
                                name = (firstName + " " + lastName).trim();
                            } else if (deal.getInitiator().getClass().getMethod("getName") != null) {
                                name = (String) deal.getInitiator().getClass()
                                    .getMethod("getName").invoke(deal.getInitiator());
                            }
                            if (name != null && !name.trim().isEmpty()) {
                                creatorName = name;
                            }
                        } catch (Exception e) {
                            // If reflection fails, use toString() or default
                            creatorName = deal.getInitiator().toString();
                        }
                    }
                    dealInfo.put("creatorName", creatorName);
                    
                    return dealInfo;
                })
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
            @RequestParam Long userId) {
        try {
            // Check if user has already joined this deal using service layer
            GroupDeal deal = groupDealService.getDealById(dealId);
            boolean alreadyJoined = deal.getMembers() != null && 
                                 deal.getMembers().stream()
                                     .anyMatch(m -> m.getUser() != null && 
                                                  m.getUser().getUserId() != null && 
                                                  m.getUser().getUserId().equals(userId));
            
            if (alreadyJoined) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "You have already joined this deal"
                ));
            }
            
            // Join the deal (will be added to group_members with approved=false)
            groupDealService.joinGroupDeal(dealId, userId);
            
            // Get updated deal info
            deal = groupDealService.getDealById(dealId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Successfully requested to join the deal");
            response.put("memberCount", deal.getMembers() != null ? deal.getMembers().size() : 0);
            response.put("maxMembers", deal.getMaxMembers() != null ? deal.getMaxMembers() : 10);
            response.put("dealId", dealId);
            response.put("userId", userId);
            response.put("approved", false); // New joins are pending approval
            
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
    public ResponseEntity<String> leaveDeal(@PathVariable Long dealId, @RequestParam Long userId) {
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
    public ResponseEntity<List<Map<String, Object>>> getAllDealsForUser(@PathVariable Long userId) {
        try {
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
                boolean isCreator = deal.getInitiator() != null && deal.getInitiator().getUserId() != null && deal.getInitiator().getUserId().equals(userId);
                Long creatorId = deal.getInitiator() != null ? deal.getInitiator().getUserId() : null;

                Integer maxMembers = deal.getMaxMembers();
                if (maxMembers == null && deal.getProduct() != null) {
                    maxMembers = deal.getProduct().getDealMaxMembers();
                }

                Map<String, Object> m = new HashMap<>();
                m.put("id", deal.getId());
                m.put("productId", deal.getProduct() != null ? deal.getProduct().getId() : null);
                m.put("productName", deal.getProduct() != null ? deal.getProduct().getName() : null);
                m.put("memberCount", deal.getMembers() != null ? deal.getMembers().size() : 0);
                m.put("maxMembers", maxMembers != null ? maxMembers : 10);
                m.put("status", deal.getStatus());
                m.put("createdAt", deal.getCreatedAt());
                m.put("creatorId", creatorId);
                m.put("isCreator", isCreator);
                m.put("isMember", true);
                m.put("userRole", isCreator ? "CREATOR" : "MEMBER");
                out.add(m);
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
    public ResponseEntity<List<GroupDeal>> getDealsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(groupDealService.getDealsByUser(userId));
    }

    // Get deals joined by a specific user
    @GetMapping("/joined/{userId}")
    public ResponseEntity<List<GroupDeal>> getDealsJoinedByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(groupDealService.getDealsJoinedByUser(userId));
    }

    // Test endpoint to verify API is working
    @GetMapping("/test/{userId}")
    public ResponseEntity<Map<String, Object>> testUserEndpoint(@PathVariable Long userId) {
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

    // Approve member join request - updates group_member table
    @PostMapping("/{dealId}/approve/{userId}")
    public ResponseEntity<Map<String, Object>> approveMember(@PathVariable Long dealId, @PathVariable Long userId) {
        try {
            GroupMember approvedMember = groupDealService.approveMember(dealId, userId);

            GroupDeal deal = groupDealService.getDealById(dealId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Member approved successfully");
            response.put("dealId", dealId);
            response.put("userId", userId);
            response.put("approved", true);
            response.put("memberCount", deal.getMembers() != null ? deal.getMembers().size() : 0);
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
    public ResponseEntity<Map<String, Object>> rejectMember(@PathVariable Long dealId, @PathVariable Long userId) {
        try {
            groupDealService.rejectMember(dealId, userId);

            GroupDeal deal = groupDealService.getDealById(dealId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Member rejected and removed from deal");
            response.put("dealId", dealId);
            response.put("userId", userId);
            response.put("memberCount", deal.getMembers() != null ? deal.getMembers().size() : 0);
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
    public ResponseEntity<List<Map<String, Object>>> getPendingRequests(@PathVariable Long dealId) {
        try {
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