package com.works.JessHsu.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * 給瀏覽器用的 CORS 規則
 * - 允許 localhost:5173 走跨域 AJAX
 * - 允許帶 cookie (session)
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration c = new CorsConfiguration();

        // 允許的前端 origin，要精準列出，有幾個就列幾個
        c.setAllowedOrigins(List.of(
            "http://localhost:5173",
            "http://localhost:3000"
        ));

        // 允許的方法
        c.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));

        // 允許攜帶的 request headers
        c.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));

        // 回應時哪些 header 可以被瀏覽器 JS 讀到（可加可不加）
        c.setExposedHeaders(List.of("Location"));

        // 關鍵：要讓 cookie (JSESSIONID) 可以跨域傳遞
        c.setAllowCredentials(true);

        // 預檢快取秒數
        c.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource s = new UrlBasedCorsConfigurationSource();
        // 套用到後端全部 API
        s.registerCorsConfiguration("/**", c);
        return s;
    }
}