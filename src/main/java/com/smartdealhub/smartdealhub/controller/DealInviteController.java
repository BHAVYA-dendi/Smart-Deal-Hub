package com.smartdealhub.smartdealhub.controller;

import com.smartdealhub.smartdealhub.model.DealInvite;
import com.smartdealhub.smartdealhub.model.InviteStatus;
import com.smartdealhub.smartdealhub.service.DealInviteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/deal-invite")
@RequiredArgsConstructor
public class DealInviteController {

    private final DealInviteService dealInviteService;

    // ================== Send Invite ==================
    @PostMapping("/send")
    public ResponseEntity<DealInvite> sendInvite(@RequestParam Long dealId,
                                                 @RequestParam Long senderId,
                                                 @RequestParam Long receiverId) {
        DealInvite invite = dealInviteService.sendInvite(dealId, senderId, receiverId);
        return ResponseEntity.ok(invite);
    }

    // ================== Get Invites by Receiver ==================
    @GetMapping("/user/{receiverId}")
    public ResponseEntity<List<DealInvite>> getInvites(@PathVariable Long receiverId) {
        List<DealInvite> invites = dealInviteService.getInvitesByReceiver(receiverId);
        return ResponseEntity.ok(invites);
    }

    // ================== Update Status ==================
    @PutMapping("/{inviteId}/status")
    public ResponseEntity<DealInvite> updateStatus(@PathVariable Long inviteId,
                                                   @RequestParam InviteStatus status) {
        return dealInviteService.updateStatus(inviteId, status)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ================== Get Invites by Group Deal ==================
    @GetMapping("/group/{dealId}")
    public ResponseEntity<List<DealInvite>> getGroupInvites(@PathVariable Long dealId) {
        List<DealInvite> invites = dealInviteService.getInvitesByGroupDeal(dealId);
        return ResponseEntity.ok(invites);
    }
}