package com.smartdealhub.smartdealhub.dto;

import java.util.List;

public class RouteResponse {
    private List<RouteStep> steps;
    private Double totalDistance;
    private String summary;

    // ===== Constructors =====
    public RouteResponse() {}

    // Existing full constructor
    public RouteResponse(List<RouteStep> steps, Double totalDistance, String summary) {
        this.steps = steps;
        this.totalDistance = totalDistance;
        this.summary = summary;
    }

    // 🔹 New: if controller passes (String summary, List<RouteStep> steps)
    public RouteResponse(String summary, List<RouteStep> steps) {
        this.summary = summary;
        this.steps = steps;
    }

    // 🔹 New: if controller passes (int count, double distance, List<RouteStep> steps)
    public RouteResponse(int count, double totalDistance, List<RouteStep> steps) {
        this.summary = count + " stores found";
        this.totalDistance = totalDistance;
        this.steps = steps;
    }

    // ===== Getters & Setters =====
    public List<RouteStep> getSteps() {
        return steps;
    }

    public void setSteps(List<RouteStep> steps) {
        this.steps = steps;
    }

    public Double getTotalDistance() {
        return totalDistance;
    }

    public void setTotalDistance(Double totalDistance) {
        this.totalDistance = totalDistance;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}