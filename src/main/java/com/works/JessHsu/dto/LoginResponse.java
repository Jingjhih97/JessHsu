package com.works.JessHsu.dto;

public class LoginResponse {
    private String username;
    private boolean success;
    private String token;
    private int remainingTries;

    public LoginResponse() {}

    public LoginResponse(String username, boolean success, String token, int remainingTries) {
        this.username = username;
        this.success = success;
        this.token = token;
        this.remainingTries = remainingTries;
    }

    public String getUsername() { return username; }
    public boolean isSuccess() { return success; }
    public String getToken() { return token; }
    public int getRemainingTries() { return remainingTries; }

    public void setUsername(String username) { this.username = username; }
    public void setSuccess(boolean success) { this.success = success; }
    public void setToken(String token) { this.token = token; }
    public void setRemainingTries(int remainingTries) { this.remainingTries = remainingTries; }
}