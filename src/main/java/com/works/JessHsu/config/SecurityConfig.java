package com.works.JessHsu.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.works.JessHsu.security.AdminAuthFilter;
import com.works.JessHsu.security.AdminUserDetailsService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final AdminUserDetailsService adminUserDetailsService;
    private final AdminAuthFilter adminAuthFilter;

    public SecurityConfig(AdminUserDetailsService adminUserDetailsService,
                          AdminAuthFilter adminAuthFilter) {
        this.adminUserDetailsService = adminUserDetailsService;
        this.adminAuthFilter = adminAuthFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {})
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        // CORS 預檢
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 前台公開 API
                        .requestMatchers(HttpMethod.GET, "/api/themes/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/portfolioItems/**").permitAll()

                        // 登入 / 登出 API
                        .requestMatchers(HttpMethod.POST, "/api/admin/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/admin/auth/logout").permitAll()

                        // 其它 admin API 必須有 Bearer token
                        .requestMatchers("/api/admin/**").authenticated()

                        // 其它先全部放行
                        .anyRequest().permitAll()
                )
                // 不用預設 form login / http basic
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable());

        // ❗ 在 UsernamePasswordAuthenticationFilter 之前加上我們的 Bearer token filter
        http.addFilterBefore(adminAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}