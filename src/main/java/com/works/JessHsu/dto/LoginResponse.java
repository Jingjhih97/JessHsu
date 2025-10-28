package com.works.JessHsu.dto;

public class LoginResponse {
    private String username;
    private boolean ok;
    private String token;

    public LoginResponse() {}

    public LoginResponse(String username, boolean ok, String token) {
        this.username = username;
        this.ok = ok;
        this.token = token;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public boolean isOk() { return ok; }
    public void setOk(boolean ok) { this.ok = ok; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}