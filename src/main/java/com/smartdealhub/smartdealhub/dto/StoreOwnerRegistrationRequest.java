package com.smartdealhub.smartdealhub.dto;

import com.smartdealhub.smartdealhub.model.Store;
import com.smartdealhub.smartdealhub.model.User;

public class StoreOwnerRegistrationRequest {
    private String name;
    private String email;
    private String password;
    private String phone;

    // Store fields
    private String storeName;
    private String storeType; // ONLINE or OFFLINE
    private String city;
    private String state;
    private Double latitude;
    private Double longitude;

    public StoreOwnerRegistrationRequest() {}

    // Getters & Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }
    public String getStoreType() { return storeType; }
    public void setStoreType(String storeType) { this.storeType = storeType; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
}
