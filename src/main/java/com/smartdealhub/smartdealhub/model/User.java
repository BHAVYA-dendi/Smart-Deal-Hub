package com.smartdealhub.smartdealhub.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JoinColumn(name="user_id")
    private Long userId;
    @JoinColumn(name="name")
    @Column(nullable = false)
    private String name;
    @JoinColumn(name="email")
    @Column(nullable = false, unique = true)
    private String email;
    @JoinColumn(name="password")
    @Column(nullable = false)
    private String password;

    @JoinColumn(name="phone")
    private String phone;

    @Enumerated(EnumType.STRING)
    @JoinColumn(name="role")
    @Column(nullable = false)
    private Role role = Role.USER;
    @JoinColumn(name="created_at")
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private double savings;
    @Column(nullable = false)
    private boolean loggedIn = false;
    @Column(nullable = false)
    private boolean active = true;

    // Relationships
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Store> stores = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Review> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Favorite> favorites = new ArrayList<>();

    @OneToMany(mappedBy = "initiator", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<GroupDeal> groupDealsInitiated = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<GroupMember> groupMemberships = new ArrayList<>();

    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<DealInvite> sentInvites = new ArrayList<>();

    @OneToMany(mappedBy = "receiver", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<DealInvite> receivedInvites = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Notification> notifications = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Feedback> feedbacks = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<VisitedStore> visitedStores = new ArrayList<>();

    // Role Enum
    public enum Role {
        USER, STORE_OWNER, ADMIN
    }

    // Constructors
    public User() {}
    public User(String name, String email, String password, String phone, Role role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.role = role;
        this.createdAt = LocalDateTime.now();
    }

    // Getters & Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public double getSavings() { return savings; }
    public void setSavings(double savings) { this.savings = savings; }
    public boolean isLoggedIn() { return loggedIn; }
    public void setLoggedIn(boolean loggedIn) { this.loggedIn = loggedIn; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public List<Store> getStores() { return stores; }
    public void setStores(List<Store> stores) { this.stores = stores; }
    public List<Review> getReviews() { return reviews; }
    public void setReviews(List<Review> reviews) { this.reviews = reviews; }
    public List<Favorite> getFavorites() { return favorites; }
    public void setFavorites(List<Favorite> favorites) { this.favorites = favorites; }
    public List<GroupDeal> getGroupDealsInitiated() { return groupDealsInitiated; }
    public void setGroupDealsInitiated(List<GroupDeal> groupDealsInitiated) { this.groupDealsInitiated = groupDealsInitiated; }
    public List<GroupMember> getGroupMemberships() { return groupMemberships; }
    public void setGroupMemberships(List<GroupMember> groupMemberships) { this.groupMemberships = groupMemberships; }
    public List<DealInvite> getSentInvites() { return sentInvites; }
    public void setSentInvites(List<DealInvite> sentInvites) { this.sentInvites = sentInvites; }
    public List<DealInvite> getReceivedInvites() { return receivedInvites; }
    public void setReceivedInvites(List<DealInvite> receivedInvites) { this.receivedInvites = receivedInvites; }
    public List<Notification> getNotifications() { return notifications; }
    public void setNotifications(List<Notification> notifications) { this.notifications = notifications; }
    public List<Feedback> getFeedbacks() { return feedbacks; }
    public void setFeedbacks(List<Feedback> feedbacks) { this.feedbacks = feedbacks; }
    public List<VisitedStore> getVisitedStores() { return visitedStores; }
    public void setVisitedStores(List<VisitedStore> visitedStores) { this.visitedStores = visitedStores; }

    // Utility Methods
    public void addStore(Store store) {
        stores.add(store);
        store.setOwner(this);
    }

    public void removeStore(Store store) {
        stores.remove(store);
        store.setOwner(null);
    }

    public void addReview(Review review) {
        reviews.add(review);
        review.setUser(this);
    }

    public void addFavorite(Favorite favorite) {
        favorites.add(favorite);
        favorite.setUser(this);
    }
}