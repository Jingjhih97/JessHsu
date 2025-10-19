package com.works.JessHsu.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.works.JessHsu.entity.PortfolioItemImage;

public interface PortfolioItemImageRepository extends JpaRepository<PortfolioItemImage, Long> {

    // ✅ 依關聯欄位命名（item.id）
    List<PortfolioItemImage> findByItem_IdOrderBySortOrderAscIdAsc(Long itemId);

    Optional<PortfolioItemImage> findFirstByItem_IdAndIsPrimaryTrue(Long itemId);

    // 取該 item 目前最大 sort_order（新增時接在最後用）
    @Query("SELECT MAX(i.sortOrder) FROM PortfolioItemImage i WHERE i.item.id = :itemId")
    Integer findMaxSort(@Param("itemId") Long itemId);

    // 清除其他主圖（保留指定 imageId 為主圖）
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE PortfolioItemImage i SET i.isPrimary = false WHERE i.item.id = :itemId AND i.id <> :imageId AND i.isPrimary = true")
    int clearOtherPrimary(@Param("itemId") Long itemId, @Param("imageId") Long imageId);

    List<PortfolioItemImage> findByItem_IdOrderByIsPrimaryDescSortOrderAscIdAsc(Long itemId);

}