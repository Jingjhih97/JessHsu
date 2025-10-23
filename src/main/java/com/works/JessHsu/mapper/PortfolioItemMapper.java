package com.works.JessHsu.mapper;

import com.works.JessHsu.dto.PortfolioItemCreateDTO;
import com.works.JessHsu.dto.PortfolioItemDTO;
import com.works.JessHsu.entity.PortfolioItem;
import com.works.JessHsu.entity.PortfolioItemImage;

public class PortfolioItemMapper {
    public static PortfolioItem toEntity(PortfolioItemCreateDTO dto) {
        PortfolioItem e = new PortfolioItem();
        e.setTitle(dto.getTitle());
        e.setDescription(dto.getDescription());
        e.setCoverImageUrl(dto.getCoverImageUrl());
        e.setCategory(dto.getCategory());
        e.setPublished(dto.getPublished() != null ? dto.getPublished() : false);
        return e;
    }

    public static void updateEntity(PortfolioItem e, PortfolioItemCreateDTO dto) {
        if (dto.getTitle() != null)
            e.setTitle(dto.getTitle());
        if (dto.getDescription() != null)
            e.setDescription(dto.getDescription());
        if (dto.getCoverImageUrl() != null)
            e.setCoverImageUrl(dto.getCoverImageUrl());
        if (dto.getCategory() != null)
            e.setCategory(dto.getCategory());
        if (dto.getPublished() != null)
            e.setPublished(dto.getPublished());
    }

    public static PortfolioItemDTO toDTO(PortfolioItem e) {
        PortfolioItemDTO d = new PortfolioItemDTO();
        d.setId(e.getId());
        d.setTitle(e.getTitle());
        d.setDescription(e.getDescription());
        d.setCoverImageUrl(e.getCoverImageUrl());
        d.setCategory(e.getCategory());
        d.setPublished(e.getPublished());
        d.setCreatedAt(e.getCreatedAt());
        d.setUpdatedAt(e.getUpdatedAt());

        PortfolioItemImage cover = e.getCoverItemImage();
        if (cover != null) {
            d.setCoverCropX(cover.getCropX());
            d.setCoverCropY(cover.getCropY());
            d.setCoverCropSize(cover.getCropSize());
        }

        return d;

    }
}
