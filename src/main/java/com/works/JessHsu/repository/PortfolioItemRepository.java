package com.works.JessHsu.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.works.JessHsu.entity.PortfolioItem;
import com.works.JessHsu.repository.view.PortfolioCardView;

public interface PortfolioItemRepository extends JpaRepository<PortfolioItem, Long> {

    /* ----------------- 基本查詢：已發佈 ----------------- */
    Page<PortfolioItem> findByPublishedTrue(Pageable pageable);

    /* ----------------- 基本查詢：指定分類且已發佈 ----------------- */
    Page<PortfolioItem> findByCategoryAndPublishedTrue(String category, Pageable pageable);

    /* ----------------- 一般清單（以 display_order DESC -> created_at DESC 排序） ----------------- */
    @Query("""
        SELECT i
        FROM PortfolioItem i
        WHERE (:onlyPublished IS NULL OR :onlyPublished = FALSE OR i.published = true)
          AND (:category IS NULL OR :category = '' OR i.category = :category)
        ORDER BY i.displayOrder DESC, i.createdAt DESC
    """)
    Page<PortfolioItem> search(
            @Param("onlyPublished") Boolean onlyPublished,
            @Param("category") String category,
            Pageable pageable
    );

    /* ----------------- Cards（前台卡片牆；以 display_order DESC -> created_at DESC 排序） ----------------- */
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
            ORDER BY i.display_order DESC, i.created_at DESC
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

    /* ======================== 顯示序工具（用於「向前遞補」） ======================== */

    /** 取得目前最大的 displayOrder（新增時可設為 max+1 => 放最前） */
    @Query("SELECT COALESCE(MAX(i.displayOrder), 0) FROM PortfolioItem i")
    Integer findMaxDisplayOrder();

    /**
     * 刪除某筆後，把「顯示序 > removedOrder」的通通 -1，達到向前遞補。
     * 建議在 Service 的 delete() 裡先查出被刪那筆的 displayOrder，再呼叫此方法。
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE PortfolioItem i SET i.displayOrder = i.displayOrder - 1 WHERE i.displayOrder > :removedOrder")
    int compactDisplayOrderAfter(@Param("removedOrder") int removedOrder);
}