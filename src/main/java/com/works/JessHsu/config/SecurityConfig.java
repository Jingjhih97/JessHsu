// src/main/java/com/works/JessHsu/config/SecurityConfig.java
package com.works.JessHsu.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())                 // Postman 測試 API 必關
            .cors(Customizer.withDefaults())              // 若前端會跨域
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.GET, "/api/**").permitAll()   // GET 放行
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()   // 預檢請求
                .requestMatchers("/actuator/**").permitAll()
                .anyRequest().authenticated()                                // 其餘(POST/PUT/DELETE) 要登入
            )
            .httpBasic(Customizer.withDefaults());         // 一定要開 Basic Auth
        return http.build();
    }

    // 固定一組好記的測試帳密：admin / 123456
    @Bean
    public UserDetailsService users(PasswordEncoder pe) {
        UserDetails admin = User.withUsername("admin")
                .password(pe.encode("123456"))
                .roles("ADMIN")
                .build();
        return new InMemoryUserDetailsManager(admin);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}