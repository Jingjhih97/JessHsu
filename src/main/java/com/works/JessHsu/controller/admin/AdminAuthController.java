package com.works.JessHsu.controller.admin;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.works.JessHsu.security.AdminSessionStore;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {

    private final AuthenticationManager authenticationManager;
    private final AdminSessionStore sessionStore;

    public AdminAuthController(
            AuthenticationManager authenticationManager,
            AdminSessionStore sessionStore
    ) {
        this.authenticationManager = authenticationManager;
        this.sessionStore = sessionStore;
    }

    public record LoginRequest(String username, String password) {}

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest req,
            HttpServletRequest request
    ) {
        try {
            UsernamePasswordAuthenticationToken token =
                    new UsernamePasswordAuthenticationToken(req.username(), req.password());

            Authentication auth = authenticationManager.authenticate(token);
            String username = auth.getName();

            // 發一組 Bearer token
            String bearerToken = sessionStore.issueToken(username);

            return ResponseEntity.ok(Map.of(
                    "message", "login success",
                    "username", username,
                    "token", bearerToken,
                    "expiresIn", sessionStore.getTtlSeconds()
            ));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body(
                    Map.of("message", "帳號或密碼錯誤")
            );
        }
    }

    public record LogoutRequest(String token) {}

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody LogoutRequest req) {
        sessionStore.invalidate(req.token());
        return ResponseEntity.ok(Map.of("message", "logout success"));
    }
}