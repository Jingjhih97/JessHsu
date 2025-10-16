// src/main/java/com/works/JessHsu/repository/PortfolioItemRepository.java
package com.works.JessHsu.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.works.JessHsu.dto.PortfolioCardDTO;
import com.works.JessHsu.entity.PortfolioItem;

public interface PortfolioItemRepository extends JpaRepository<PortfolioItem, Long> {

  /* --------------- 分頁：完整 Item（可選擇 onlyPublished／category） --------------- */
  @Query("""
          SELECT p FROM PortfolioItem p
          WHERE (:onlyPublished IS NULL OR p.published = :onlyPublished)
            AND (:category IS NULL OR p.category = :category)
          ORDER BY p.createdAt DESC
      """)
  Page<PortfolioItem> search(@Param("onlyPublished") Boolean onlyPublished,
      @Param("category") String category,
      Pageable pageable);

  /* --------------- 分頁：卡片清單（每作品 1 張主圖） --------------- */
  @Query(value = """
          SELECT new com.works.JessHsu.dto.PortfolioCardDTO(
            p.id, p.title,
            COALESCE(
              MAX(CASE WHEN img.isPrimary = true THEN img.imageUrl END),
              MAX(img.imageUrl),
              ''
            )
          )
          FROM PortfolioItem p
          LEFT JOIN p.images img
          WHERE (:onlyPublished IS NULL OR p.published = :onlyPublished)
            AND (:category IS NULL OR p.category = :category)
          GROUP BY p.id, p.title
          ORDER BY MAX(p.createdAt) DESC
      """, countQuery = """
          SELECT COUNT(p)
          FROM PortfolioItem p
          WHERE (:onlyPublished IS NULL OR p.published = :onlyPublished)
            AND (:category IS NULL OR p.category = :category)
      """)
  Page<PortfolioCardDTO> searchCards(@Param("onlyPublished") Boolean onlyPublished,
      @Param("category") String category,
      Pageable pageable);

  /* ---------------（可保留的簡易方法：如果你有地方直接用到） --------------- */
  Page<PortfolioItem> findByPublishedTrue(Pageable pageable);

  Page<PortfolioItem> findByCategoryAndPublishedTrue(String category, Pageable pageable);
}