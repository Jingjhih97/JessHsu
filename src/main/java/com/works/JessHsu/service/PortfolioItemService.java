package com.works.JessHsu.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.works.JessHsu.dto.PortfolioCardDto;
import com.works.JessHsu.dto.PortfolioItemCreateDTO;
import com.works.JessHsu.dto.PortfolioItemDTO; // ★ 新增 import
import com.works.JessHsu.dto.PortfolioItemDetailDTO;

public interface PortfolioItemService {

    PortfolioItemDTO create(PortfolioItemCreateDTO dto);

    PortfolioItemDTO update(Long id, PortfolioItemCreateDTO dto);

    void delete(Long id);

    PortfolioItemDTO get(Long id);

    Page<PortfolioItemDTO> list(Pageable pageable, Boolean onlyPublished, String category);

    PortfolioItemDetailDTO getDetail(Long id);

    // ★ 新增：前台卡片牆（封面圖）列表
    Page<PortfolioCardDto> listCards(Pageable pageable, Boolean onlyPublished, String category);
}