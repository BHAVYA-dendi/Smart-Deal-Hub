package com.smartdealhub.smartdealhub.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreAnalyticsResponse {

    // Total number of products in the store
    private int totalProducts;

    // Total number of active offers
    private int totalOffers;

    // Total visits across all the owner's stores
    private int totalStoreVisits;

    // Map of productId -> number of views or purchases
    private Map<Long, Integer> productViews;

    // Map of productId -> average rating
    private Map<Long, Double> productRatings;

    // Total number of group deals created
    private int totalGroupDeals;

    // Total users who joined group deals
    private int totalGroupDealMembers;
}