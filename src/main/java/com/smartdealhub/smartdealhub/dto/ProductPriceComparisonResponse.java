package com.smartdealhub.smartdealhub.dto;

import java.util.List;

// ===== Response for product price comparison =====
public class ProductPriceComparisonResponse {

    private String routePath;            // e.g., "Store A -> Store B -> Store C"
    private List<RouteStep> routeSteps;  // Detailed info for each step/store

    // ===== Constructors =====
    public ProductPriceComparisonResponse() {}

    public ProductPriceComparisonResponse(String routePath, List<RouteStep> routeSteps) {
        this.routePath = routePath;
        this.routeSteps = routeSteps;
    }

    // ===== Getters & Setters =====
    public String getRoutePath() {
        return routePath;
    }

    public void setRoutePath(String routePath) {
        this.routePath = routePath;
    }

    public List<RouteStep> getRouteSteps() {
        return routeSteps;
    }

    public void setRouteSteps(List<RouteStep> routeSteps) {
        this.routeSteps = routeSteps;
    }
}