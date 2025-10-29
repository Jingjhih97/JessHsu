// src/main/java/com/works/JessHsu/service/PortfolioItemService.java
package com.works.JessHsu.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.works.JessHsu.dto.ImageUpsertDTO;
import com.works.JessHsu.dto.PortfolioCardDto;
import com.works.JessHsu.dto.PortfolioImageCreateDTO;
import com.works.JessHsu.dto.PortfolioImageCropDTO;
import com.works.JessHsu.dto.PortfolioImageDTO;
import com.works.JessHsu.dto.PortfolioImageOrderUpdateDTO;
import com.works.JessHsu.dto.PortfolioItemCreateDTO;
import com.works.JessHsu.dto.PortfolioItemDTO;
import com.works.JessHsu.dto.PortfolioItemDetailDTO;

public interface PortfolioItemService {

    // CRUD
    PortfolioItemDTO create(PortfolioItemCreateDTO dto);
    PortfolioItemDTO update(Long id, PortfolioItemCreateDTO dto);
    void delete(Long id);
    PortfolioItemDTO get(Long id);
    Page<PortfolioItemDTO> list(Pageable pageable, Boolean onlyPublished, String category, Long themeId, String q);

    // 詳細頁
    PortfolioItemDetailDTO getDetail(Long id);

    // 前台卡片牆（封面圖）列表
    Page<PortfolioCardDto> listCards(Pageable pageable, Boolean onlyPublished, String category, Long themeId);

    List<PortfolioImageDTO> listImages(Long itemId);

    PortfolioImageDTO addImage(Long itemId, PortfolioImageCreateDTO dto);

    void removeImage(Long itemId, Long imageId);

    void setPrimaryImage(Long itemId, Long imageId);

    void reorderImages(Long itemId, List<PortfolioImageOrderUpdateDTO> orders);

    /** 🔹 新增這個方法，對應 setPublished() 控制上架/下架 */
    void setPublished(Long itemId, boolean published);

    void updateImageUrl(Long itemId, Long imageId, String newUrl);

    void updateImageCrop(Long itemId, Long imageId, PortfolioImageCropDTO crop);

    void replaceImages(Long itemId, List<ImageUpsertDTO> images);
}