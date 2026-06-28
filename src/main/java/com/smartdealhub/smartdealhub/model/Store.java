package com.smartdealhub.smartdealhub.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "stores")
@JsonIgnoreProperties({"products","storeHours","favorites","visitedStores"})
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JoinColumn(name = "store_id", nullable = false)
    private Long storeId;

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @JoinColumn(name = "store_type", nullable = false)
    @Column(nullable = false)
    private StoreType storeType;

    private String city;
    private String state;
    private Double latitude;
    private Double longitude;
    @JoinColumn(name = "created_at")
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Relationships
    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Product> products = new ArrayList<>();

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StoreHour> storeHours = new ArrayList<>();

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Favorite> favorites = new ArrayList<>();

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VisitedStore> visitedStores = new ArrayList<>();

    // Enum for StoreType
    public enum StoreType {
        ONLINE, OFFLINE
    }

    // Constructors
    public Store() {}

    public Store(User owner, String name, StoreType storeType, String city, String state, Double latitude, Double longitude) {
        this.owner = owner;
        this.name = name;
        this.storeType = storeType;
        this.city = city;
        this.state = state;
        this.latitude = latitude;
        this.longitude = longitude;
        this.createdAt = LocalDateTime.now();
    }

    // Getters & Setters
    public Long getStoreId() { return storeId; }
    public void setStoreId(Long storeId) { this.storeId = storeId; }
    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public StoreType getStoreType() { return storeType; }
    public void setStoreType(StoreType storeType) { this.storeType = storeType; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public List<Product> getProducts() { return products; }
    public void setProducts(List<Product> products) { this.products = products; }
    public List<StoreHour> getStoreHours() { return storeHours; }
    public void setStoreHours(List<StoreHour> storeHours) { this.storeHours = storeHours; }
    public List<Favorite> getFavorites() { return favorites; }
    public void setFavorites(List<Favorite> favorites) { this.favorites = favorites; }
    public List<VisitedStore> getVisitedStores() { return visitedStores; }
    public void setVisitedStores(List<VisitedStore> visitedStores) { this.visitedStores = visitedStores; }

    // Utility Methods
    public void addProduct(Product product) {
        products.add(product);
        product.setStore(this);
    }

    public void removeProduct(Product product) {
        products.remove(product);
        product.setStore(null);
    }

    public void addStoreHour(StoreHour hour) {
        storeHours.add(hour);
        hour.setStore(this);
    }

    public void removeStoreHour(StoreHour hour) {
        storeHours.remove(hour);
        hour.setStore(null);
    }
}