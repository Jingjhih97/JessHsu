package com.works.JessHsu.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration c = new CorsConfiguration();

        c.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "https://yoodesigns.com",
                "https://www.yoodesigns.com"
                //"http://18.209.58.255",
                //"http://api.yoodesigns.com"
                ));

        // 允許跨域時夾帶 cookie / session
        c.setAllowCredentials(true);

        // 允許的方法
        c.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // 前端可以送的 header
        c.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin"));

        // 回應時前端可讀到的 header（可留空或列一些）
        c.setExposedHeaders(List.of("Location"));

        // 預檢快取（秒）
        c.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 套到所有路徑
        source.registerCorsConfiguration("/**", c);
        return source;
    }
}