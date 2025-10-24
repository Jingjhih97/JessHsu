package com.works.JessHsu.service.impl;

import java.util.Optional;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.works.JessHsu.entity.AdminUser;
import com.works.JessHsu.repository.AdminUserRepository;
import com.works.JessHsu.service.AuthService;

@Service
public class AuthServiceImpl implements AuthService {

    private final AdminUserRepository repo;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthServiceImpl(AdminUserRepository repo) {
        this.repo = repo;
    }

    @Override
    public AdminUser login(String username, String rawPassword) {
        Optional<AdminUser> opt = repo.findByUsername(username);
        if (opt.isEmpty()) return null;
        AdminUser user = opt.get();

        boolean match = encoder.matches(rawPassword, user.getPasswordHash());
        if (!match) return null;

        // 如果你以後想做停用，就在這裡判斷 user.getRole() 或 isActive 之類
        return user;
    }
}