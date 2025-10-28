package com.works.JessHsu.controller.admin;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.works.JessHsu.dto.LoginRequest;
import com.works.JessHsu.dto.LoginResponse;
import com.works.JessHsu.entity.AdminUser;
import com.works.JessHsu.security.AdminSessionStore;
import com.works.JessHsu.service.AdminAuthService;

@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {

    private final AdminAuthService authService;
    private final AdminSessionStore sessionStore;

    public AdminAuthController(AdminAuthService authService, AdminSessionStore sessionStore) {
        this.authService = authService;
        this.sessionStore = sessionStore;
    }

    /**
     * 登入：
     * 1. 驗證帳密
     * 2. 產生 token 並存進 AdminSessionStore
     * 3. 回傳 { token, username }
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest body) {
        AdminUser user = authService.login(body.getUsername(), body.getPassword());
        if (user == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("帳號或密碼錯誤");
        }

        String token = sessionStore.createSessionFor(user.getId());

        LoginResponse resp = new LoginResponse();
        resp.setUsername(user.getUsername());
        resp.setOk(true);
        resp.setToken(token);

        return ResponseEntity.ok(resp);
    }

    /**
     * 登出（可選）
     * 前端要把 token 傳上來（Authorization: Bearer ...）
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        String token = extractToken(authHeader);
        sessionStore.invalidate(token);
        return ResponseEntity.ok().build();
    }

    /**
     * 給前端檢查目前 token 還活著嗎
     */
    @GetMapping("/me")
    public ResponseEntity<?> me(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        String token = extractToken(authHeader);
        Long adminId = sessionStore.validate(token);
        if (adminId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("未登入或已過期");
        }
        return ResponseEntity.ok(adminId);
    }

    private String extractToken(String authHeader) {
        if (authHeader == null) return null;
        if (!authHeader.startsWith("Bearer ")) return null;
        return authHeader.substring(7).trim();
    }
}