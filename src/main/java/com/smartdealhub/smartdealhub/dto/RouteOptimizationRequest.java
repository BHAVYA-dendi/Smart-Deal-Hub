package com.smartdealhub.smartdealhub.dto;

import java.util.List;

public class RouteOptimizationRequest {
    private List<Integer> storeIds; // IDs of stores to visit
    private String startLocation;   // Optional: starting point (lat/lon as "lat,lon")

    // Getters and Setters
    public List<Integer> getStoreIds() { return storeIds; }
    public void setStoreIds(List<Integer> storeIds) { this.storeIds = storeIds; }
    public String getStartLocation() { return startLocation; }
    public void setStartLocation(String startLocation) { this.startLocation = startLocation; }
}