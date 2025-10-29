package com.works.JessHsu.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.works.JessHsu.entity.Theme;

public interface ThemeRepository extends JpaRepository<Theme, Long> {

    boolean existsByName(String name);

    List<Theme> findAllByOrderByNameAsc();
}