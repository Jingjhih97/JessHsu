package com.works.JessHsu.security;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 這個 Filter 會在進入 Controller 之前跑：
 * - 只保護 /api/admin/** 的路由
 * - 但 /api/admin/auth/login 例外（因為那是拿 token 的入口）
 *
 * 流程：
 * 1. 把 Authorization header 拿出來
 * 2. 把 Bearer <token> 解析出 token
 * 3. 丟給 AdminSessionStore.resolve(token)
 *    - 回 null 表示沒登入或過期
 * 4. 沒通過就直接回 401
 */
public class AdminAuthFilter implements Filter {

    private final AdminSessionStore sessionStore;

    public AdminAuthFilter(AdminSessionStore sessionStore) {
        this.sessionStore = sessionStore;
    }

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException {

        HttpServletRequest req  = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String path = req.getRequestURI();
        String method = req.getMethod();

        // 1. CORS 預檢 (OPTIONS) 直接放
        if ("OPTIONS".equalsIgnoreCase(method)) {
            chain.doFilter(request, response);
            return;
        }

        // 2. 只保護 /api/admin/** 底下的東西
        //    /api/admin/auth/login 仍需允許未登入呼叫 (拿 token 的地方)
        boolean isAdminApi = path.startsWith("/api/admin/");
        boolean isLoginApi = path.equals("/api/admin/auth/login");

        if (isAdminApi && !isLoginApi) {
            String authHeader = req.getHeader("Authorization");
            String token = extractToken(authHeader);

            Long adminId = sessionStore.resolve(token);

            if (adminId == null) {
                // token 不存在 / 過期 / header 壞掉
                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
                res.setContentType("text/plain;charset=UTF-8");
                res.getWriter().write("未授權或登入已過期");
                return;
            }

            // （可選）在這裡把 adminId 存進 request attribute
            // 之後 Controller 如果想知道是誰登入的，可以拿 req.getAttribute("adminId")
            req.setAttribute("adminId", adminId);
        }

        // 3. 放行
        chain.doFilter(request, response);
    }

    private String extractToken(String authHeader) {
        if (authHeader == null) return null;
        if (!authHeader.startsWith("Bearer ")) return null;
        return authHeader.substring("Bearer ".length()).trim();
    }
}