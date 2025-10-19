package com.works.JessHsu.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.works.JessHsu.entity.Admin;

public interface AdminRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findByUsername(String username);

    boolean existsByUsername(String username);
}
