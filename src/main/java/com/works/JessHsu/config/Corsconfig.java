// src/main/java/com/works/JessHsu/config/CorsConfig.java
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

        // 用 EXACT origin（不能用 * 且要含 port）
        c.setAllowedOrigins(List.of("http://localhost:5173"));

        // 允許你會用到的方法
        c.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));

        // 允許的自訂 header（至少要包含 Authorization、Content-Type）
        c.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));

        // 若你有需要在前端讀取 Location 等 header，可曝露：
        c.setExposedHeaders(List.of("Location"));

        // 只有在你要帶 cookie（withCredentials=true）時設為 true；否則 false 也可以
        // 你目前用 Basic Auth（放在 Authorization header），不是 cookie，
        // 所以不需要 credentials；保持 false 也 OK
        c.setAllowCredentials(false);

        // 預檢結果快取（秒）
        c.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource s = new UrlBasedCorsConfigurationSource();
        s.registerCorsConfiguration("/**", c);
        return s;
    }
}