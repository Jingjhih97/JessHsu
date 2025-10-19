// src/main/java/com/works/JessHsu/repository/PortfolioItemRepository.java
package com.works.JessHsu.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.works.JessHsu.entity.PortfolioItem;

public interface PortfolioItemRepository extends JpaRepository<PortfolioItem, Long> {

    // 基本查詢：已發佈
    Page<PortfolioItem> findByPublishedTrue(Pageable pageable);

    // 基本查詢：指定分類且已發佈
    Page<PortfolioItem> findByCategoryAndPublishedTrue(String category, Pageable pageable);

    /* ----------------- 一般清單用（ServiceImpl 的 list() 會呼叫這支） ----------------- */
    @Query("""
        SELECT i
        FROM PortfolioItem i
        WHERE (:onlyPublished IS NULL OR :onlyPublished = FALSE OR i.published = true)
          AND (:category IS NULL OR :category = '' OR i.category = :category)
        ORDER BY i.createdAt DESC
    """)
    Page<PortfolioItem> search(
            @Param("onlyPublished") Boolean onlyPublished,
            @Param("category") String category,
            Pageable pageable
    );

    /* ----------------- Cards (前台卡片牆用，固定依 created_at DESC 排序) ----------------- */
    @Query(
        value = """
            SELECT
              i.id AS id,
              i.title AS title,
              i.category AS category,
              COALESCE(
                (SELECT pii.image_url
                   FROM portfolio_item_images pii
                  WHERE pii.item_id = i.id AND pii.is_primary = 1
                  LIMIT 1),
                (SELECT pii2.image_url
                   FROM portfolio_item_images pii2
                  WHERE pii2.item_id = i.id
                  ORDER BY pii2.sort_order ASC, pii2.id ASC
                  LIMIT 1)
              ) AS cover_image_url,
              i.created_at AS created_at
            FROM portfolio_items i
            WHERE
              (:onlyPublished IS NULL OR :onlyPublished = FALSE OR i.published = 1)
              AND (:category IS NULL OR :category = '' OR i.category = :category)
            ORDER BY i.created_at DESC
        """,
        countQuery = """
            SELECT COUNT(1)
            FROM portfolio_items i
            WHERE
              (:onlyPublished IS NULL OR :onlyPublished = FALSE OR i.published = 1)
              AND (:category IS NULL OR :category = '' OR i.category = :category)
        """,
        nativeQuery = true
    )
    Page<PortfolioCardView> searchCardsNative(
            @Param("onlyPublished") Boolean onlyPublished,
            @Param("category") String category,
            Pageable pageable
    );
}