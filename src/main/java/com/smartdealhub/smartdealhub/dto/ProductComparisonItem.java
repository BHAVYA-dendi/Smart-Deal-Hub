package com.smartdealhub.smartdealhub.dto;

public class ProductComparisonItem {
    private Long productId;
    private String productName;
    private Double price;
    private String storeName;
    private String storeType;
    private String storeCity;
    private String storeState;
    private Double storeLatitude;
    private Double storeLongitude;

    public ProductComparisonItem() {}

    public ProductComparisonItem(Long productId, String productName, Double price, String storeName, 
                                  String storeType, String storeCity, String storeState, 
                                  Double storeLatitude, Double storeLongitude) {
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.storeName = storeName;
        this.storeType = storeType;
        this.storeCity = storeCity;
        this.storeState = storeState;
        this.storeLatitude = storeLatitude;
        this.storeLongitude = storeLongitude;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getStoreType() {
        return storeType;
    }

    public void setStoreType(String storeType) {
        this.storeType = storeType;
    }

    public String getStoreCity() {
        return storeCity;
    }

    public void setStoreCity(String storeCity) {
        this.storeCity = storeCity;
    }

    public String getStoreState() {
        return storeState;
    }

    public void setStoreState(String storeState) {
        this.storeState = storeState;
    }

    public Double getStoreLatitude() {
        return storeLatitude;
    }

    public void setStoreLatitude(Double storeLatitude) {
        this.storeLatitude = storeLatitude;
    }

    public Double getStoreLongitude() {
        return storeLongitude;
    }

    public void setStoreLongitude(Double storeLongitude) {
        this.storeLongitude = storeLongitude;
    }
}
