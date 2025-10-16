package com.works.JessHsu.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        // 允許來源（建議用 allowedOriginPatterns，更彈性）
                        .allowedOriginPatterns("http://localhost:5173", "http://localhost:3000")
                        // 允許方法
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                        // 允許所有標頭
                        .allowedHeaders("*")
                        // 是否允許攜帶 Cookie / Authorization
                        .allowCredentials(true)
                        // 預檢請求的快取時間（秒）
                        .maxAge(3600);
            }
        };
    }
}
