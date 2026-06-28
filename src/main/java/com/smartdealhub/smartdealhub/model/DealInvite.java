package com.smartdealhub.smartdealhub.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "deal_invites")
public class DealInvite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invite_id")
    private Long id;

    // Many invites belong to one group deal
    @ManyToOne
    @JoinColumn(name = "deal_id", nullable = false)
    private GroupDeal deal;

    // Many invites are sent by a user
    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    // Many invites are received by a user
    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    // Invite status
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InviteStatus status = InviteStatus.PENDING;

    @Column(name = "sent_at")
    private LocalDateTime sentAt = LocalDateTime.now();

    // ===== Constructors =====
    public DealInvite() {}

    public DealInvite(GroupDeal deal, User sender, User receiver) {
        this.deal = deal;
        this.sender = sender;
        this.receiver = receiver;
        this.status = InviteStatus.PENDING;
        this.sentAt = LocalDateTime.now();
    }

    // ===== Getters & Setters =====
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public GroupDeal getDeal() { return deal; }
    public void setDeal(GroupDeal deal) { this.deal = deal; }
    public User getSender() { return sender; }
    public void setSender(User sender) { this.sender = sender; }
    public User getReceiver() { return receiver; }
    public void setReceiver(User receiver) { this.receiver = receiver; }
    public InviteStatus getStatus() { return status; }
    public void setStatus(InviteStatus status) { this.status = status; }
    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
}