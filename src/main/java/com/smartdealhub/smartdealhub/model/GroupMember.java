package com.smartdealhub.smartdealhub.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "group_members")
public class GroupMember {

    @EmbeddedId
    private GroupMemberKey id;

    @ManyToOne
    @MapsId("dealId")
    @JoinColumn(name = "deal_id")
    private GroupDeal deal;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt = LocalDateTime.now();

    private boolean approved = false;

    // ===== Constructors =====
    public GroupMember() {}

    public GroupMember(GroupDeal deal, User user) {
        this.deal = deal;
        this.user = user;
        this.id = new GroupMemberKey(deal.getId(), user.getUserId());
    }

    // ===== Getters & Setters =====
    public GroupMemberKey getId() { return id; }
    public void setId(GroupMemberKey id) { this.id = id; }

    public GroupDeal getDeal() { return deal; }
    public void setDeal(GroupDeal deal) { this.deal = deal; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public LocalDateTime getJoinedAt() { return joinedAt; }
    public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }

    public boolean isApproved() { return approved; }
    public void setApproved(boolean approved) { this.approved = approved; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GroupMember)) return false;
        GroupMember that = (GroupMember) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}