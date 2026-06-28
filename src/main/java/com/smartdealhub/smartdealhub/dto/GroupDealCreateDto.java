package com.smartdealhub.smartdealhub.dto;

import java.math.BigDecimal;

public class GroupDealCreateDto {
    private Long productId;
    private Long initiatorId;
    private String title;
    private String description;
    private Integer discountPercent;
    private Integer maxMembers;
    private BigDecimal dealPrice;

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public Long getInitiatorId() { return initiatorId; }
    public void setInitiatorId(Long initiatorId) { this.initiatorId = initiatorId; }

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
}
