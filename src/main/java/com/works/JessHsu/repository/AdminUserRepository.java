package com.works.JessHsu.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.works.JessHsu.entity.AdminUser;

public interface AdminUserRepository extends JpaRepository<AdminUser, Long> {
    Optional<AdminUser> findByUsername(String username);
}