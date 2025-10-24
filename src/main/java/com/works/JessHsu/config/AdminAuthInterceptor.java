package com.works.JessHsu.config;

import org.springframework.web.servlet.HandlerInterceptor;

import com.works.JessHsu.controller.admin.AuthController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class AdminAuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws Exception {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(AuthController.SESSION_KEY) == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write("未授權，請先登入");
            return false;
        }

        return true; // 放行
    }
}