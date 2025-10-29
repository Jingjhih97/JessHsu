// src/main/java/com/works/JessHsu/config/SecurityConfig.java
package com.works.JessHsu.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            // 讓 Spring Security 用我們在 CorsConfig 裡定義的 CorsConfigurationSource
            .cors(Customizer.withDefaults())

            // 我們是前後端分離 + AJAX，所以先關 CSRF
            .csrf(csrf -> csrf.disable())

            .authorizeHttpRequests(auth -> auth
                // 預檢請求 (OPTIONS) 必須允許，否則 preflight 直接卡死
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // 開放登入 API
                .requestMatchers("/api/auth/login").permitAll()

                // 開放公開瀏覽的前台資料
                .requestMatchers(
                        "/uploads/**",
                        "/api/portfolioItems/**"
                ).permitAll()

                // 其他都先 allow（真正的後台保護你可以用攔截器或之後的 token filter）
                .anyRequest().permitAll()
            )

            // 關掉 spring security 預設的 login prompt
            .httpBasic(b -> b.disable())
            .formLogin(f -> f.disable())
            .logout(l -> l.disable());

        return http.build();
    }
}