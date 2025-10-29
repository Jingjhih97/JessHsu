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

  /* ===================== 基本查詢 ===================== */

  Page<PortfolioItem> findByPublishedTrue(Pageable pageable);

  Page<PortfolioItem> findByCategoryAndPublishedTrue(String category, Pageable pageable);

  /**
   * 後台列表 /admin：支援
   * - onlyPublished
   * - category (文字分類)
   * - q (模糊搜尋 id/title/category)
   * - themeId (主題下拉)
   *
   * Pageable 會決定排序 (displayOrder desc, createdAt desc ...等都可以在 service/controller
   * 傳進來)
   */
  @Query("""
      SELECT i
      FROM PortfolioItem i
      WHERE (:onlyPublished IS NULL OR :onlyPublished = FALSE OR i.published = true)
        AND (:category IS NULL OR :category = '' OR i.category = :category)
        AND (:themeId IS NULL OR i.theme.id = :themeId)
        AND (
          :q IS NULL OR :q = '' OR
          CAST(i.id AS string) LIKE CONCAT('%', :q, '%') OR
          LOWER(i.title) LIKE LOWER(CONCAT('%', :q, '%')) OR
          LOWER(i.category) LIKE LOWER(CONCAT('%', :q, '%'))
        )
      """)
  Page<PortfolioItem> search(
      @Param("onlyPublished") Boolean onlyPublished,
      @Param("category") String category,
      @Param("q") String q,
      @Param("themeId") Long themeId,
      Pageable pageable);

  /**
   * 前台卡片牆：回傳精簡資訊 (PortfolioCardView)
   * 排序固定：display_order DESC, created_at DESC
   *
   * 也一併支援 themeId 過濾
   */
  @Query(value = """
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
        AND (:themeId IS NULL OR i.theme_id = :themeId)
      ORDER BY i.display_order DESC, i.created_at DESC
      """, countQuery = """
      SELECT COUNT(1)
      FROM portfolio_items i
      WHERE
        (:onlyPublished IS NULL OR :onlyPublished = FALSE OR i.published = 1)
        AND (:category IS NULL OR :category = '' OR i.category = :category)
        AND (:themeId IS NULL OR i.theme_id = :themeId)
      """, nativeQuery = true)
  Page<PortfolioCardView> searchCardsNative(
      @Param("onlyPublished") Boolean onlyPublished,
      @Param("category") String category,
      @Param("themeId") Long themeId,
      Pageable pageable);

  /* ===================== 顯示序工具 ===================== */

  @Query("SELECT COALESCE(MAX(i.displayOrder), 0) FROM PortfolioItem i")
  Integer findMaxDisplayOrder();

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("UPDATE PortfolioItem i SET i.displayOrder = i.displayOrder - 1 WHERE i.displayOrder > :removedOrder")
  int compactDisplayOrderAfter(@Param("removedOrder") int removedOrder);

  /* ===================== Theme 使用次數 ===================== */

  /**
   * ThemeController.listAll() 用來算 usageCount
   * ex: itemRepo.countByTheme_Id(themeId)
   */
  long countByTheme_Id(Long themeId);
}