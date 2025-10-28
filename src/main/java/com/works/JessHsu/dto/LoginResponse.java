package com.works.JessHsu.dto;

public class LoginResponse {

    private String username;
    private boolean success;
    private String token; // 如果你現在還沒用 token，可以先留 null

    // ✅ 無參數建構子（必要給 Jackson）
    public LoginResponse() {}

    // ✅ 兩個參數版本：目前控制器用的
    public LoginResponse(String username, boolean success) {
        this.username = username;
        this.success = success;
        this.token = null;
    }

    // ✅ 三個參數版本：以後 token 模式會用到
    public LoginResponse(String username, boolean success, String token) {
        this.username = username;
        this.success = success;
        this.token = token;
    }

    // Getter / Setter
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}