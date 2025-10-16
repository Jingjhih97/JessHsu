package com.works.JessHsu.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.works.JessHsu.entity.PortfolioItem;

public interface PortfolioItemRepository extends JpaRepository<PortfolioItem, Long> {
  Page<PortfolioItem> findByPublishedTrue(Pageable pageable);
  Page<PortfolioItem> findByCategoryAndPublishedTrue(String category, Pageable pageable);
}
