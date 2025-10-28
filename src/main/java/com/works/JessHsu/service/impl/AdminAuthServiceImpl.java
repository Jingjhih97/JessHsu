package com.works.JessHsu.service.impl;

import java.util.Optional;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.works.JessHsu.entity.AdminUser;
import com.works.JessHsu.repository.AdminUserRepository;
import com.works.JessHsu.service.AdminAuthService;

@Service
public class AdminAuthServiceImpl implements AdminAuthService {

    private final AdminUserRepository repo;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AdminAuthServiceImpl(AdminUserRepository repo) {
        this.repo = repo;
    }

    @Override
    public AdminUser login(String username, String rawPassword) {
        Optional<AdminUser> opt = repo.findByUsername(username);
        if (opt.isEmpty()) {
            return null;
        }

        AdminUser user = opt.get();
        boolean match = encoder.matches(rawPassword, user.getPasswordHash());
        if (!match) {
            return null;
        }

        // 之後如果你有 "停用" 權限，你可以在這裡擋
        return user;
    }
}