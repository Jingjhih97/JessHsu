package com.works.JessHsu.service.impl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.works.JessHsu.dto.PortfolioCardDto;
import com.works.JessHsu.dto.PortfolioImageDTO;
import com.works.JessHsu.dto.PortfolioItemCreateDTO;
import com.works.JessHsu.dto.PortfolioItemDTO;
import com.works.JessHsu.dto.PortfolioItemDetailDTO;
import com.works.JessHsu.entity.PortfolioItem;
import com.works.JessHsu.entity.PortfolioItemImage;
import com.works.JessHsu.exception.NotFoundException;
import com.works.JessHsu.mapper.PortfolioItemMapper;
import com.works.JessHsu.repository.PortfolioItemImageRepository;
import com.works.JessHsu.repository.PortfolioItemRepository;
import com.works.JessHsu.repository.view.PortfolioCardView;
import com.works.JessHsu.service.PortfolioItemService;


public class PortfolioItemServiceImpl implements PortfolioItemService {

    private final PortfolioItemRepository repo;
    private final PortfolioItemImageRepository imageRepo;

    // ✅ 手動建構子注入（無 Lombok）
    public PortfolioItemServiceImpl(PortfolioItemRepository repo, PortfolioItemImageRepository imageRepo) {
        this.repo = repo;
        this.imageRepo = imageRepo;
    }

    /* ----------------- CRUD ----------------- */

    @Override
    public PortfolioItemDTO create(PortfolioItemCreateDTO dto) {
        PortfolioItem e = PortfolioItemMapper.toEntity(dto);
        e = itemRepo.save(e);
        return PortfolioItemMapper.toDTO(e);
    }

    @Override
    public PortfolioItemDTO update(Long id, PortfolioItemCreateDTO dto) {
        PortfolioItem e = itemRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("PortfolioItem " + id + " not found"));
        PortfolioItemMapper.updateEntity(e, dto);
        e = itemRepo.save(e);
        return PortfolioItemMapper.toDTO(e);
    }

    @Override
    public void delete(Long id) {
        // 先確認存在，不存在直接 404
        if (!repo.existsById(id)) {
            throw new NotFoundException("PortfolioItem " + id + " not found");
        }
        itemRepo.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public PortfolioItemDTO get(Long id) {
        PortfolioItem e = itemRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("PortfolioItem " + id + " not found"));
        return PortfolioItemMapper.toDTO(e);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PortfolioItemDTO> list(Pageable pageable, Boolean onlyPublished, String category) {
        if (Boolean.TRUE.equals(onlyPublished) && category != null && !category.isBlank()) {
            return repo.findByCategoryAndPublishedTrue(category, pageable)
                    .map(PortfolioItemMapper::toDTO);
        }
        if (Boolean.TRUE.equals(onlyPublished)) {
            return repo.findByPublishedTrue(pageable)
                    .map(PortfolioItemMapper::toDTO);
        }
        return repo.findAll(pageable).map(PortfolioItemMapper::toDTO);
    }

    /* ----------------- Cards (前台卡片牆用) ----------------- */

    @Override
    @Transactional(readOnly = true)
    public Page<PortfolioCardDto> listCards(Pageable pageable, Boolean onlyPublished, String category) {
        Page<PortfolioCardView> page = repo.searchCardsNative(onlyPublished, category, pageable);
        return page.map(v -> new PortfolioCardDto(
                v.getId(),
                v.getTitle(),
                v.getCategory(),
                v.getCoverImageUrl(),
                v.getCreatedAt() == null ? null : v.getCreatedAt().toLocalDateTime()
        ));
    }

    /* ----------------- Detail (前台作品詳細頁) ----------------- */

    @Override
    @Transactional(readOnly = true)
    public PortfolioItemDetailDTO getDetail(Long id) {
        // 取得作品
        PortfolioItem item = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("PortfolioItem " + id + " not found"));

        // 取得圖片（主圖優先）
        var images = imageRepo.findByItem_IdOrderByIsPrimaryDescSortOrderAscIdAsc(id);

        List<PortfolioImageDTO> imageDTOs = images.stream()
                .map(i -> new PortfolioImageDTO(
                        i.getId(),
                        item.getId(),
                        i.getImageUrl(),
                        Boolean.TRUE.equals(i.getIsPrimary()),
                        i.getSortOrder(),
                        i.getCreatedAt()))
                .toList();

        return new PortfolioItemDetailDTO(
                item.getId(),
                item.getTitle(),
                item.getDescription(),
                item.getCategory(),
                item.getPublished(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                imageDTOs
        );
    }
}