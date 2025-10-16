// src/main/java/com/works/JessHsu/service/PortfolioItemService.java
package com.works.JessHsu.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.works.JessHsu.dto.PortfolioCardDTO;
import com.works.JessHsu.dto.PortfolioImageCreateDTO;
import com.works.JessHsu.dto.PortfolioImageDTO;
import com.works.JessHsu.dto.PortfolioImageOrderUpdateDTO;
import com.works.JessHsu.dto.PortfolioItemCreateDTO;
import com.works.JessHsu.dto.PortfolioItemDTO;

public interface PortfolioItemService {

    /* ----------------- 既有 CRUD ----------------- */
    PortfolioItemDTO create(PortfolioItemCreateDTO dto);

    PortfolioItemDTO update(Long id, PortfolioItemCreateDTO dto);

    void delete(Long id);

    PortfolioItemDTO get(Long id);

    Page<PortfolioItemDTO> list(Pageable pageable, Boolean onlyPublished, String category);

    /* ----------------- 新增：卡片清單（每作品一張主圖） ----------------- */
    /**
     * 卡片清單：每個作品只回一張主圖（若無主圖則回排序第一張或空字串），
     * 適合首頁圖片牆使用。
     */
    Page<PortfolioCardDTO> listCards(Pageable pageable, Boolean onlyPublished, String category);

    /* ----------------- 新增：圖片管理（方案 B） ----------------- */

    List<PortfolioImageDTO> listImages(Long itemId);

    PortfolioImageDTO addImage(Long itemId, PortfolioImageCreateDTO dto);

    void removeImage(Long itemId, Long imageId);

    void setPrimaryImage(Long itemId, Long imageId);

    void reorderImages(Long itemId, List<PortfolioImageOrderUpdateDTO> orders);

    void setPublished(Long itemId, boolean published);
}