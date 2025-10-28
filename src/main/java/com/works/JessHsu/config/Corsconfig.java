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
            "http://localhost:5173"
        ));

        c.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));

        // 關鍵：前端會帶 Authorization: Bearer xxx
        c.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));

        // 後端不需要回 cookie，所以不用 credentials
        c.setAllowCredentials(false);

        c.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource s = new UrlBasedCorsConfigurationSource();
        s.registerCorsConfiguration("/**", c);
        return s;
    }
}