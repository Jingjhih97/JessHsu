package com.works.JessHsu.controller.admin;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.works.JessHsu.dto.LoginRequest;
import com.works.JessHsu.dto.LoginResponse;
import com.works.JessHsu.entity.AdminUser;
import com.works.JessHsu.service.AuthService;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    public static final String SESSION_KEY = "ADMIN_AUTH";

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /** 登入 */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest body, HttpSession session) {
        String username = body.getUsername();
        String password = body.getPassword();

        AdminUser user = authService.login(username, password);
        if (user == null) {
            // 驗證失敗
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("帳號或密碼錯誤");
        }

        // 驗證成功，寫入 session
        session.setAttribute(SESSION_KEY, user.getId());

        return ResponseEntity.ok(new LoginResponse(user.getUsername(), true));
    }

    /** 登出 */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok().build();
    }

    /** 檢查目前是否已登入（前端可用來做守門） */
    @GetMapping("/me")
    public ResponseEntity<?> me(HttpSession session) {
        Object uid = session.getAttribute(SESSION_KEY);
        if (uid == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("未登入");
        }
        return ResponseEntity.ok(uid);
    }
}