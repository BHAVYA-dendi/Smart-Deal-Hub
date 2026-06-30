package com.smartdealhub.smartdealhub.controller;

import com.smartdealhub.smartdealhub.model.DealInvite;
import com.smartdealhub.smartdealhub.model.GroupDeal;
import com.smartdealhub.smartdealhub.model.InviteStatus;
import com.smartdealhub.smartdealhub.model.User;
import com.smartdealhub.smartdealhub.service.DealInviteService;
import com.smartdealhub.smartdealhub.service.GroupDealService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/deal-invite")
@RequiredArgsConstructor
public class DealInviteController {

    private final DealInviteService dealInviteService;
    private final GroupDealService groupDealService;

    private User requireCurrentUser(HttpServletRequest request) {
        Object cu = request.getAttribute("currentUser");
        if (cu instanceof User user) return user;
        throw new RuntimeException("Unauthenticated");
    }

    @PostMapping("/send")
    public ResponseEntity<DealInvite> sendInvite(@RequestParam Long dealId,
                                                 @RequestParam Long senderId,
                                                 @RequestParam Long receiverId,
                                                 HttpServletRequest request) {
        User current = requireCurrentUser(request);
        if (current.getRole() != User.Role.ADMIN && !current.getUserId().equals(senderId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        DealInvite invite = dealInviteService.sendInvite(dealId, senderId, receiverId);
        return ResponseEntity.ok(invite);
    }

    @GetMapping("/user/{receiverId}")
    public ResponseEntity<List<DealInvite>> getInvites(@PathVariable Long receiverId, HttpServletRequest request) {
        User current = requireCurrentUser(request);
        if (current.getRole() != User.Role.ADMIN && !current.getUserId().equals(receiverId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        List<DealInvite> invites = dealInviteService.getInvitesByReceiver(receiverId);
        return ResponseEntity.ok(invites);
    }

    @PutMapping("/{inviteId}/status")
    public ResponseEntity<DealInvite> updateStatus(@PathVariable Long inviteId,
                                                   @RequestParam InviteStatus status,
                                                   HttpServletRequest request) {
        User current = requireCurrentUser(request);
        return dealInviteService.updateStatus(inviteId, status, current.getUserId(), current.getRole() == User.Role.ADMIN)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/group/{dealId}")
    public ResponseEntity<List<DealInvite>> getGroupInvites(@PathVariable Long dealId, HttpServletRequest request) {
        User current = requireCurrentUser(request);
        GroupDeal deal = groupDealService.getDealById(dealId);
        boolean allowed = current.getRole() == User.Role.ADMIN
                || (deal.getInitiator() != null && current.getUserId().equals(deal.getInitiator().getUserId()));
        if (!allowed) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        List<DealInvite> invites = dealInviteService.getInvitesByGroupDeal(dealId);
        return ResponseEntity.ok(invites);
    }
}
