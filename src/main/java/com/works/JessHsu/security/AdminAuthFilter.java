package com.works.JessHsu.security;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class AdminAuthFilter implements Filter {

    private final AdminSessionStore sessionStore;

    public AdminAuthFilter(AdminSessionStore sessionStore) {
        this.sessionStore = sessionStore;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req  = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String path = req.getRequestURI();

        // 只保護 /api/admin/**，但 /api/admin/auth/login 例外
        if (path.startsWith("/api/admin/") && !path.equals("/api/admin/auth/login")) {

            String authHeader = req.getHeader("Authorization");
            String token = extractToken(authHeader);
            Long adminId = sessionStore.validate(token);

            if (adminId == null) {
                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
                res.setContentType("text/plain;charset=UTF-8");
                res.getWriter().write("未授權或登入已過期");
                return;
            }
        }

        // 通過
        chain.doFilter(request, response);
    }

    private String extractToken(String authHeader) {
        if (authHeader == null) return null;
        if (!authHeader.startsWith("Bearer ")) return null;
        return authHeader.substring(7).trim();
    }
}