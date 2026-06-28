package com.smartdealhub.smartdealhub.dto;

public class ChangePasswordRequest {
    private String password;
    private String newPassword;
    // getters/setters
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}
