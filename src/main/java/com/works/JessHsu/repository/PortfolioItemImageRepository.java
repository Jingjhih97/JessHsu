package com.works.JessHsu.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.works.JessHsu.entity.PortfolioItemImage;

public interface PortfolioItemImageRepository extends JpaRepository<PortfolioItemImage, Long> {

    List<PortfolioItemImage> findByItemIdOrderBySortOrderAscIdAsc(Long itemId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE PortfolioItemImage i SET i.isPrimary = FALSE WHERE i.item.id = :itemId AND i.id <> :keepId")
    int clearOtherPrimary(@Param("itemId") Long itemId, @Param("keepId") Long keepId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE PortfolioItemImage i SET i.isPrimary = FALSE WHERE i.item.id = :itemId")
    int clearAllPrimary(@Param("itemId") Long itemId);

    @Query("SELECT COALESCE(MAX(i.sortOrder), -1) FROM PortfolioItemImage i WHERE i.item.id = :itemId")
    Integer findMaxSort(@Param("itemId") Long itemId);
}