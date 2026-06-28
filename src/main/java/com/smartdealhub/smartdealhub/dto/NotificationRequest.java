package com.smartdealhub.smartdealhub.dto;

public class NotificationRequest {
    private String message;
    private int storeId;  // use int instead of String for storeId

    public NotificationRequest() {}

    public NotificationRequest(String message, int storeId) {
        this.message = message;
        this.storeId = storeId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getStoreId() {
        return storeId;
    }

    public void setStoreId(int storeId) {
        this.storeId = storeId;
    }
}