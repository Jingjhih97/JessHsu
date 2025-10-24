package com.works.JessHsu.service;

import com.works.JessHsu.entity.AdminUser;

public interface AuthService {
    AdminUser login(String username, String rawPassword);
}