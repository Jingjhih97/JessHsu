package com.works.JessHsu.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

import com.works.JessHsu.security.AdminAuthFilter;
import com.works.JessHsu.security.AdminSessionStore;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, AdminSessionStore sessionStore) throws Exception {

        http
            .cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable())

            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // 前台公開
                .requestMatchers(
                        "/uploads/**",
                        "/api/portfolioItems/**"
                ).permitAll()

                // 後台登入 API 可以匿名呼叫
                .requestMatchers("/api/admin/auth/login").permitAll()

                // 其他照樣先 permitAll
                .anyRequest().permitAll()
            )

            // 關掉預設表單/Basic，不要彈出瀏覽器登入框
            .httpBasic(basic -> basic.disable())
            .formLogin(form -> form.disable())
            .logout(logout -> logout.disable());

        // 把我們自訂 Bearer token filter 插在 Spring Security 的 anonymous 之前
        http.addFilterBefore(
            new AdminAuthFilter(sessionStore),
            AnonymousAuthenticationFilter.class
        );

        return http.build();
    }
}