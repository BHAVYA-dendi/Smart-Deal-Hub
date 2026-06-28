package com.smartdealhub.smartdealhub.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "feedback")
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({
            "stores","reviews","favorites","groupDealsInitiated","groupMemberships",
            "sentInvites","receivedInvites","notifications","feedbacks","visitedStores","password"
    })
    private User user;

    @Column(name = "message", nullable = false)
    private String message;

    @Column(name = "subject")
    private String subject;

    @Column(name = "rating")
    private Integer rating;

    @Column(name = "reply", columnDefinition = "TEXT")
    private String reply;

    @Column(name = "visible")
    private Boolean visible = true;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum Status { PENDING, REVIEWED, RESOLVED }
    @ManyToOne
    @JoinColumn(name = "store_id")
    @JsonIgnoreProperties({"owner","products","offers","reviews","deals"})
    private Store store;

    // + getter & setter
    public Store getStore() { return store; }
    public void setStore(Store store) { this.store = store; }

    // ===== Constructors =====
    public Feedback() {}
    public Feedback(User user, String message, String subject) {
        this.user = user;
        this.message = message;
        this.subject = subject;
        this.status = Status.PENDING;
        this.createdAt = LocalDateTime.now();
        this.visible = true;
    }

    // ===== Getters & Setters =====
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    public String getReply() { return reply; }
    public void setReply(String reply) { this.reply = reply; }
    public Boolean getVisible() { return visible; }
    public void setVisible(Boolean visible) { this.visible = visible; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}