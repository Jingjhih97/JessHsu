package com.works.JessHsu.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.works.JessHsu.repository.AdminRepository;

@Configuration
public class SecurityConfig {

    // 允許的前端來源（預設 Vite 5173）
    @Value("${app.cors.allowed-origins[0]:http://localhost:5173}")
    private String allowedOrigin0;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 用資料庫的 Admin 表當成帳號來源
     * 需要你有 AdminRepository 與 Admin Entity（含 username、passwordHash、role）
     */
    @Bean
    public UserDetailsService userDetailsService(AdminRepository repo) {
        return username -> repo.findByUsername(username)
                .map(a -> User.withUsername(a.getUsername())
                        .password(a.getPasswordHash())
                        .roles(a.getRole()) // 例如 "ADMIN" -> 會自動加 ROLE_ADMIN
                        .build())
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(
            UserDetailsService uds, BCryptPasswordEncoder encoder) {
        DaoAuthenticationProvider p = new DaoAuthenticationProvider();
        p.setUserDetailsService(uds);
        p.setPasswordEncoder(encoder);
        return p;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CORS（允許前端帶 Cookie）
                .cors(cors -> cors.configurationSource(req -> {
                    var c = new org.springframework.web.cors.CorsConfiguration();
                    c.setAllowedOrigins(List.of(allowedOrigin0));
                    c.addAllowedHeader("*");
                    c.addAllowedMethod("*");
                    c.setAllowCredentials(true);
                    return c;
                }))
                // 開發期先關 CSRF；若上線建議改為啟用並配合 CSRF token
                .csrf(csrf -> csrf.disable())
                // Session 模式
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                // 授權規則
                .authorizeHttpRequests(auth -> auth
                        // 登入開放、登出需登入
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/logout").authenticated()

                        // 公開資源
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/actuator/**").permitAll()

                        // 管理員保護路由
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // 其他都要登入
                        .anyRequest().authenticated())
                // 我們用自訂 JSON 登入（AuthController），所以把內建表單與 Basic 關掉
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable());

        return http.build();
    }
}