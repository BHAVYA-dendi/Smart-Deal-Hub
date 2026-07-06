package com.smartdealhub.smartdealhub.dto;

import java.util.List;

// ===== Response for product price comparison =====
public class ProductPriceComparisonResponse {

    private String productName;
    private List<ProductComparisonItem> products;
    private Double lowestPrice;
    private Double highestPrice;
    private Double averagePrice;
    private Integer storeCount;

    // ===== Constructors =====
    public ProductPriceComparisonResponse() {}

    public ProductPriceComparisonResponse(String productName, List<ProductComparisonItem> products) {
        this.productName = productName;
        this.products = products;
        if (products != null && !products.isEmpty()) {
            this.lowestPrice = products.stream().mapToDouble(ProductComparisonItem::getPrice).min().orElse(0);
            this.highestPrice = products.stream().mapToDouble(ProductComparisonItem::getPrice).max().orElse(0);
            this.averagePrice = products.stream().mapToDouble(ProductComparisonItem::getPrice).average().orElse(0);
            this.storeCount = products.size();
        }
    }

    // ===== Getters & Setters =====
    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public List<ProductComparisonItem> getProducts() {
        return products;
    }

    public void setProducts(List<ProductComparisonItem> products) {
        this.products = products;
    }

    public Double getLowestPrice() {
        return lowestPrice;
    }

    public void setLowestPrice(Double lowestPrice) {
        this.lowestPrice = lowestPrice;
    }

    public Double getHighestPrice() {
        return highestPrice;
    }

    public void setHighestPrice(Double highestPrice) {
        this.highestPrice = highestPrice;
    }

    public Double getAveragePrice() {
        return averagePrice;
    }

    public void setAveragePrice(Double averagePrice) {
        this.averagePrice = averagePrice;
    }

    public Integer getStoreCount() {
        return storeCount;
    }

    public void setStoreCount(Integer storeCount) {
        this.storeCount = storeCount;
    }
}