// src/main/java/com/works/JessHsu/security/AdminUserDetails.java
package com.works.JessHsu.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.works.JessHsu.entity.AdminUser;

public class AdminUserDetails implements UserDetails {

    private final AdminUser adminUser;

    public AdminUserDetails(AdminUser admin) {
        this.adminUser = admin;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String role = adminUser.getRole();
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getPassword() {
        return adminUser.getPasswordHash(); // 用資料庫的 bcrypt 欄位
    }

    @Override
    public String getUsername() {
        return adminUser.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }

    public AdminUser getAdmin() {
        return adminUser;
    }
}