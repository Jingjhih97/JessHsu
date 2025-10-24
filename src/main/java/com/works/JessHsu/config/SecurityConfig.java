package com.works.JessHsu.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 我們目前不用 Spring Security 內建的使用者系統 / 授權規則，
 * 授權交給 AdminAuthInterceptor 來做 session 檢查。
 *
 * 這裡主要做：
 * - 關 CSRF (前後端分離 + AJAX)
 * - 開 CORS （實際 CORS 規則來自 CorsConfig）
 * - 把 httpBasic / formLogin 關掉，避免 Spring 自動跳 Basic auth 視窗
 */
@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 我們用自己的 CorsConfig，這裡啟用 security 端的 cors 支援即可
            .cors(Customizer.withDefaults())

            // 前後端分離 + session cookie，先關 CSRF
            .csrf(csrf -> csrf.disable())

            // 目前所有 request 都 allow，真正的 /api/admin/** 保護在 AdminAuthInterceptor
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/**").permitAll()
            )

            // 關掉 Security 自己的登入機制
            .httpBasic(basic -> basic.disable())
            .formLogin(form -> form.disable())
            .logout(logout -> logout.disable());

        return http.build();
    }
}