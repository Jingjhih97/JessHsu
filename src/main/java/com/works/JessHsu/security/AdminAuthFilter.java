package com.works.JessHsu.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 後台 Bearer Token 驗證：
 * - 只處理 /api/admin/** (但 /api/admin/auth/login 除外)
 * - 從 Authorization: Bearer <token> 把 token 抓出來
 * - 丟給 AdminSessionStore.resolve(token) -> username
 * - 如果有 username，就 loadUserByUsername，塞進 SecurityContext
 */
@Component
public class AdminAuthFilter extends OncePerRequestFilter {

    private final AdminSessionStore sessionStore;
    private final AdminUserDetailsService userDetailsService;

    private final RequestMatcher adminApiMatcher =
            new AntPathRequestMatcher("/api/admin/**");
    private final RequestMatcher loginMatcher =
            new AntPathRequestMatcher("/api/admin/auth/login");

    public AdminAuthFilter(AdminSessionStore sessionStore,
                           AdminUserDetailsService userDetailsService) {
        this.sessionStore = sessionStore;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // CORS 預檢直接放
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();

        // 只處理 /api/admin/**，且排除 /api/admin/auth/login
        if (!adminApiMatcher.matches(request) || loginMatcher.matches(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        String token = extractToken(authHeader);
        String username = sessionStore.resolve(token);

        if (username == null) {
            // 沒登入或 token 過期，回 401
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("""
                {"status":401,"error":"Unauthorized","message":"請重新登入後再操作"}
            """);
            return;
        }

        // 組出 Authentication 放進 SecurityContext 中
        var userDetails = userDetailsService.loadUserByUsername(username);
        var auth = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        filterChain.doFilter(request, response);
    }

    private String extractToken(String authHeader) {
        if (authHeader == null) return null;
        if (!authHeader.startsWith("Bearer ")) return null;
        return authHeader.substring("Bearer ".length()).trim();
    }
}