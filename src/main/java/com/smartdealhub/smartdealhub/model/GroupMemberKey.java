package com.smartdealhub.smartdealhub.model;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class GroupMemberKey implements Serializable {

    private Long dealId;
    private Long userId;

    public GroupMemberKey() {}

    public GroupMemberKey(Long dealId, Long userId) {
        this.dealId = dealId;
        this.userId = userId;
    }

    // ===== Getters & Setters =====
    public Long getDealId() { return dealId; }
    public void setDealId(Long dealId) { this.dealId = dealId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    // ===== Equals & HashCode =====
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GroupMemberKey)) return false;
        GroupMemberKey that = (GroupMemberKey) o;
        return Objects.equals(getDealId(), that.getDealId()) &&
                Objects.equals(getUserId(), that.getUserId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDealId(), getUserId());
    }
}