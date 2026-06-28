package com.smartdealhub.smartdealhub.dto;

// ===== Represents a single store/product step in price comparison =====
public class RouteStep {

    private Long productId;
    private String productName;
    private Double latitude;
    private Double longitude;
    private Double price;

    // ===== Constructors =====
    public RouteStep() {}

    public RouteStep(Long productId, String productName, Double latitude, Double longitude, Double price) {
        this.productId = productId;
        this.productName = productName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.price = price;
    }

    // ===== Getters & Setters =====
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

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
}