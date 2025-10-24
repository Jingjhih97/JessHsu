// LoginResponse.java
package com.works.JessHsu.dto;

public class LoginResponse {
    private String username;
    private boolean ok;

    public LoginResponse(String username, boolean ok) {
        this.username = username;
        this.ok = ok;
    }

    public String getUsername() { return username; }
    public boolean isOk() { return ok; }
}