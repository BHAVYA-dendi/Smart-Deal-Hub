package com.smartdealhub.smartdealhub.dto;

import com.smartdealhub.smartdealhub.model.Product;
import com.smartdealhub.smartdealhub.model.Store;

public class ProductSummaryDto {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private String category;
    private String brand;
    private Boolean active;
    private Boolean groupDealAllowed;
    private String dealTitle;
    private String dealDescription;
    private Integer dealDiscountPercent;
    private Integer dealMaxMembers;
    private Double dealPrice;
    private String imageUrl;
    private String createdAt;
    private StoreDto store;

    public static ProductSummaryDto from(Product p){
        ProductSummaryDto dto = new ProductSummaryDto();
        if(p == null) return dto;
        dto.id = p.getId();
        dto.name = p.getName();
        dto.description = p.getDescription();
        dto.price = p.getPrice() != null ? p.getPrice().doubleValue() : null;
        dto.category = p.getCategory();
        dto.brand = p.getBrand();
        dto.active = p.getActive();
        dto.groupDealAllowed = p.getGroupDealAllowed();
        dto.dealTitle = p.getDealTitle();
        dto.dealDescription = p.getDealDescription();
        dto.dealDiscountPercent = p.getDealDiscountPercent();
        dto.dealMaxMembers = p.getDealMaxMembers();
        dto.dealPrice = p.getDealPrice()!=null? p.getDealPrice().doubleValue(): null;
        dto.imageUrl = p.getImageUrl();
        dto.createdAt = p.getCreatedAt() != null ? p.getCreatedAt().toString() : null;
        Store s = p.getStore();
        if(s != null){
            StoreDto sd = new StoreDto();
            sd.setStoreId(s.getStoreId());
            sd.setName(s.getName());
            sd.setCity(s.getCity());
            sd.setState(s.getState());
            sd.setStoreType(s.getStoreType()!=null? s.getStoreType().name(): null);
            sd.setLatitude(s.getLatitude());
            sd.setLongitude(s.getLongitude());
            dto.store = sd;
        }
        return dto;
    }

    public static class StoreDto{
        private Long storeId;
        private String name;
        private String city;
        private String state;
        private String storeType;
        private Double latitude;
        private Double longitude;

        public Long getStoreId() { return storeId; }
        public void setStoreId(Long storeId) { this.storeId = storeId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        public String getState() { return state; }
        public void setState(String state) { this.state = state; }
        public String getStoreType() { return storeType; }
        public void setStoreType(String storeType) { this.storeType = storeType; }
        public Double getLatitude() { return latitude; }
        public void setLatitude(Double latitude) { this.latitude = latitude; }
        public Double getLongitude() { return longitude; }
        public void setLongitude(Double longitude) { this.longitude = longitude; }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
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
    public Double getDealPrice() { return dealPrice; }
    public void setDealPrice(Double dealPrice) { this.dealPrice = dealPrice; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public StoreDto getStore() { return store; }
    public void setStore(StoreDto store) { this.store = store; }
}
