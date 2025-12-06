package com.works.JessHsu.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.works.JessHsu.entity.AdminUser;
import com.works.JessHsu.repository.AdminUserRepository;

@Service
public class AdminUserDetailsService implements UserDetailsService {

    private final AdminUserRepository adminUserRepository;

    public AdminUserDetailsService(AdminUserRepository adminRepository) {
        this.adminUserRepository = adminRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        AdminUser adminUser = adminUserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Admin not found"));

        return new AdminUserDetails(adminUser);
    }
}