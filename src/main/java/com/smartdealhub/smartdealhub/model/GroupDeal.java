package com.smartdealhub.smartdealhub.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "group_deals")
public class GroupDeal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "deal_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne
    @JoinColumn(name = "initiator_id", nullable = false)
    private User initiator;

    private String title;
    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "discount_percent")
    private Integer discountPercent;

    @Column(name = "max_members")
    private Integer maxMembers;

    @Column(name = "deal_price")
    private BigDecimal dealPrice;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "status")
    private String status = "PENDING";

    @OneToMany(mappedBy = "deal", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<GroupMember> members = new HashSet<>();

    public GroupDeal() {}

    public GroupDeal(Product product, User initiator, String title, String description, Integer discountPercent, Integer maxMembers) {
        this.product = product;
        this.initiator = initiator;
        this.title = title;
        this.description = description;
        this.discountPercent = discountPercent;
        this.maxMembers = maxMembers;
    }

    // ===== Getters & Setters =====
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public User getInitiator() { return initiator; }
    public void setInitiator(User initiator) { this.initiator = initiator; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getDiscountPercent() { return discountPercent; }
    public void setDiscountPercent(Integer discountPercent) { this.discountPercent = discountPercent; }

    public Integer getMaxMembers() { return maxMembers; }
    public void setMaxMembers(Integer maxMembers) { this.maxMembers = maxMembers; }

    public BigDecimal getDealPrice() { return dealPrice; }
    public void setDealPrice(BigDecimal dealPrice) { this.dealPrice = dealPrice; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Set<GroupMember> getMembers() { return members; }
    public void setMembers(Set<GroupMember> members) { this.members = members; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // ===== Convenience Methods =====
    public void addMember(GroupMember member) {
        members.add(member);
        member.setDeal(this);
    }

    public void removeMember(GroupMember member) {
        members.remove(member);
        member.setDeal(null);
    }

    public boolean isFull() {
        return members.size() >= maxMembers;
    }
}