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
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults()) // ⭐ 啟用 CORS 與下方 Bean 整合
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // ⭐ 放行預檢
                .requestMatchers("/api/**").permitAll()                // ⭐ 放行 API
                .anyRequest().permitAll()
            )
            .httpBasic(basic -> basic.disable()) // ⭐ 停用 Basic 挑戰，避免誤回 401
            .formLogin(form -> form.disable());  // ⭐ 停用表單登入
        return http.build();
    }
}