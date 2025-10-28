package com.works.JessHsu.service;

import com.works.JessHsu.entity.AdminUser;

public interface AdminAuthService {
    
    AdminUser login(String username, String rawPassword);
}