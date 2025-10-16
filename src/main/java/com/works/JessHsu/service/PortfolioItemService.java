// src/main/java/com/works/JessHsu/service/PortfolioItemService.java
package com.works.JessHsu.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.works.JessHsu.dto.PortfolioItemCreateDTO;
import com.works.JessHsu.dto.PortfolioItemDTO;

public interface PortfolioItemService {
    PortfolioItemDTO create(PortfolioItemCreateDTO dto);

    PortfolioItemDTO update(Long id, PortfolioItemCreateDTO dto);

    void delete(Long id);

    PortfolioItemDTO get(Long id);

    Page<PortfolioItemDTO> list(Pageable pageable, Boolean onlyPublished, String category);
}