// src/main/java/com/works/JessHsu/repository/PortfolioItemImageRepository.java
package com.works.JessHsu.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.works.JessHsu.entity.PortfolioItemImage;

public interface PortfolioItemImageRepository extends JpaRepository<PortfolioItemImage, Long> {

    /** 以 sort_order → id 排序（一般列表用） */
    List<PortfolioItemImage> findByItem_IdOrderBySortOrderAscIdAsc(Long itemId);

    /** 主圖優先 → 再 sort_order → id（前台/詳細頁常用） */
    List<PortfolioItemImage> findByItem_IdOrderByIsPrimaryDescSortOrderAscIdAsc(Long itemId);

    /** 取某作品目前的主圖 */
    Optional<PortfolioItemImage> findFirstByItem_IdAndIsPrimaryTrue(Long itemId);

    /** 取該 item 最大 sort_order（新增時接到最後） */
    @Query("SELECT MAX(i.sortOrder) FROM PortfolioItemImage i WHERE i.item.id = :itemId")
    Integer findMaxSort(@Param("itemId") Long itemId);

    /** 清除同作品其他主圖（只保留指定 imageId 為主圖） */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
           UPDATE PortfolioItemImage i
              SET i.isPrimary = false
            WHERE i.item.id = :itemId
              AND i.id <> :imageId
              AND i.isPrimary = true
           """)
    int clearOtherPrimary(@Param("itemId") Long itemId, @Param("imageId") Long imageId);

    /** ✅ 檢查同作品是否已存在相同 URL（避免重複插入） */
    boolean existsByItem_IdAndImageUrl(Long itemId, String imageUrl);

    /** ✅ 取得同作品、指定 URL 的那一筆（用來設為主圖） */
    Optional<PortfolioItemImage> findByItem_IdAndImageUrl(Long itemId, String imageUrl);
}