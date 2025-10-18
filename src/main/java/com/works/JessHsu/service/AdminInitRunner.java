package com.works.JessHsu.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.works.JessHsu.entity.Admin;
import com.works.JessHsu.repository.AdminRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AdminInitRunner implements CommandLineRunner {

    private final AdminRepository repo;
    private final BCryptPasswordEncoder encoder;

    @Value("${app.admin.username}")
    private String adminUsername;

    @Value("${app.admin.password}")
    private String adminPlainPassword;

    @Override
    public void run(String... args) {
        if (!repo.existsByUsername(adminUsername)) {
            Admin admin = Admin.builder()
                    .username(adminUsername)
                    .passwordHash(encoder.encode(adminPlainPassword))
                    .role("ADMIN")
                    .build();
            repo.save(admin);
            System.out.println("[INIT] Created default admin: " + adminUsername);
        } else {
            System.out.println("[INIT] Admin exists: " + adminUsername);
        }
    }
}
