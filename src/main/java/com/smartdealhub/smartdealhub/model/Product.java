package com.smartdealhub.smartdealhub.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "products")
@JsonIgnoreProperties({"offers","reviews","priceHistoryList","groupDeals"})
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "store_id")
    private Store store;

    @Column(nullable = false)
    private String name;
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "tags", columnDefinition = "TEXT")
    private String tags;

    private String category;
    private String brand;
    private BigDecimal price;
    private Boolean active = true;

    @Column(name = "group_deal_allowed")
    private Boolean groupDealAllowed = false;

    @Column(name = "deal_title")
    private String dealTitle;
    @Column(name = "deal_description", columnDefinition = "TEXT")
    private String dealDescription;
    @Column(name = "deal_discount_percent")
    private Integer dealDiscountPercent;
    @Column(name = "deal_max_members")
    private Integer dealMaxMembers;
    @Column(name = "deal_price")
    private BigDecimal dealPrice;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // Relationships
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Offer> offers;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PriceHistory> priceHistoryList;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GroupDeal> groupDeals;

    // Constructors
    public Product() {}

    public Product(Store store, String name, String description, String category, String brand,
                   BigDecimal price, Boolean active, Boolean groupDealAllowed, String imageUrl) {
        this.store = store;
        this.name = name;
        this.description = description;
        this.category = category;
        this.brand = brand;
        this.price = price;
        this.active = active;
        this.groupDealAllowed = groupDealAllowed;
        this.imageUrl = imageUrl;
        this.createdAt = LocalDateTime.now();
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Store getStore() { return store; }
    public void setStore(Store store) { this.store = store; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public Boolean getGroupDealAllowed() { return groupDealAllowed; }
    public void setGroupDealAllowed(Boolean groupDealAllowed) { this.groupDealAllowed = groupDealAllowed; }
    public String getDealTitle() { return dealTitle; }
    public void setDealTitle(String dealTitle) { this.dealTitle = dealTitle; }
    public String getDealDescription() { return dealDescription; }
    public void setDealDescription(String dealDescription) { this.dealDescription = dealDescription; }
    public Integer getDealDiscountPercent() { return dealDiscountPercent; }
    public void setDealDiscountPercent(Integer dealDiscountPercent) { this.dealDiscountPercent = dealDiscountPercent; }
    public Integer getDealMaxMembers() { return dealMaxMembers; }
    public void setDealMaxMembers(Integer dealMaxMembers) { this.dealMaxMembers = dealMaxMembers; }
    public BigDecimal getDealPrice() { return dealPrice; }
    public void setDealPrice(BigDecimal dealPrice) { this.dealPrice = dealPrice; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public List<Offer> getOffers() { return offers; }
    public void setOffers(List<Offer> offers) { this.offers = offers; }
    public List<Review> getReviews() { return reviews; }
    public void setReviews(List<Review> reviews) { this.reviews = reviews; }
    public List<PriceHistory> getPriceHistoryList() { return priceHistoryList; }
    public void setPriceHistoryList(List<PriceHistory> priceHistoryList) { this.priceHistoryList = priceHistoryList; }
    public List<GroupDeal> getGroupDeals() { return groupDeals; }
    public void setGroupDeals(List<GroupDeal> groupDeals) { this.groupDeals = groupDeals; }
}