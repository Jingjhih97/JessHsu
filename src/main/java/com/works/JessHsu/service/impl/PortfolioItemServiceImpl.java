package com.works.JessHsu.service.impl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import com.works.JessHsu.dto.PortfolioCardDto;
import com.works.JessHsu.dto.PortfolioImageCreateDTO;
import com.works.JessHsu.dto.PortfolioImageDTO;
import com.works.JessHsu.dto.PortfolioImageOrderUpdateDTO;
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

@Service
@Transactional
public class PortfolioItemServiceImpl implements PortfolioItemService {

    private final PortfolioItemRepository repo;
    private final PortfolioItemImageRepository imageRepo;

    public PortfolioItemServiceImpl(PortfolioItemRepository repo, PortfolioItemImageRepository imageRepo) {
        this.repo = repo;
        this.imageRepo = imageRepo;
    }

    /* ----------------- CRUD ----------------- */

    @Override
    public PortfolioItemDTO create(PortfolioItemCreateDTO dto) {
        PortfolioItem entity = PortfolioItemMapper.toEntity(dto); 
        PortfolioItem saved = repo.<PortfolioItem>save(entity);
        return PortfolioItemMapper.toDTO(saved);
    }

    @Override
    public PortfolioItemDTO update(Long id, PortfolioItemCreateDTO dto) {
        PortfolioItem e = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("PortfolioItem " + id + " not found"));
        PortfolioItemMapper.updateEntity(e, dto);
        return PortfolioItemMapper.toDTO(repo.save(e));
    }

    @Override
    public void delete(Long id) {
        if (!repo.existsById(id)) {
            throw new NotFoundException("PortfolioItem " + id + " not found");
        }
        repo.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public PortfolioItemDTO get(Long id) {
        return repo.findById(id)
                .map(PortfolioItemMapper::toDTO)
                .orElseThrow(() -> new NotFoundException("PortfolioItem " + id + " not found"));
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
                v.getCreatedAt() == null ? null : v.getCreatedAt().toLocalDateTime()));
    }

    /* ----------------- Detail (前台作品詳細頁) ----------------- */

    @Override
    @Transactional(readOnly = true)
    public PortfolioItemDetailDTO getDetail(Long id) {
        PortfolioItem item = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("PortfolioItem " + id + " not found"));

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
                imageDTOs);
    }

    /* ----------------- 圖片管理（後台） ----------------- */

    @Override
    @Transactional(readOnly = true)
    public List<PortfolioImageDTO> listImages(Long itemId) {
        // 只驗證存在，不載入整個 entity
        if (!repo.existsById(itemId)) {
            throw new NotFoundException("PortfolioItem " + itemId + " not found");
        }
        return imageRepo.findByItem_IdOrderByIsPrimaryDescSortOrderAscIdAsc(itemId)
                .stream()
                .map(i -> new PortfolioImageDTO(
                        i.getId(),
                        itemId,
                        i.getImageUrl(),
                        Boolean.TRUE.equals(i.getIsPrimary()),
                        i.getSortOrder(),
                        i.getCreatedAt()))
                .toList();
    }

    @Override
    public PortfolioImageDTO addImage(Long itemId, PortfolioImageCreateDTO dto) {
        PortfolioItem item = repo.findById(itemId)
                .orElseThrow(() -> new NotFoundException("PortfolioItem " + itemId + " not found"));

        PortfolioItemImage img = new PortfolioItemImage();
        img.setItem(item);
        img.setImageUrl(dto.getImageUrl());

        // sort_order 連到最後
        Integer maxSort = imageRepo.findMaxSort(itemId);
        int nextSort = (maxSort == null ? -1 : maxSort) + 1;
        img.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : nextSort);

        boolean wantPrimary = dto.getIsPrimary() != null && dto.getIsPrimary();
        img.setIsPrimary(wantPrimary);

        img = imageRepo.save(img);

        // 若本張設為主圖，清掉同作品其它主圖
        if (wantPrimary) {
            imageRepo.clearOtherPrimary(itemId, img.getId());
        }

        return new PortfolioImageDTO(
                img.getId(),
                itemId,
                img.getImageUrl(),
                Boolean.TRUE.equals(img.getIsPrimary()),
                img.getSortOrder(),
                img.getCreatedAt());
    }

    @Override
    public void removeImage(Long itemId, Long imageId) {
        PortfolioItemImage img = imageRepo.findById(imageId)
                .orElseThrow(() -> new NotFoundException("Image " + imageId + " not found"));
        if (!img.getItem().getId().equals(itemId)) {
            throw new NotFoundException("Image " + imageId + " does not belong to item " + itemId);
        }
        imageRepo.delete(img);
    }

    @Override
    public void setPrimaryImage(Long itemId, Long imageId) {
        PortfolioItemImage img = imageRepo.findById(imageId)
                .orElseThrow(() -> new NotFoundException("Image " + imageId + " not found"));
        if (!img.getItem().getId().equals(itemId)) {
            throw new NotFoundException("Image " + imageId + " does not belong to item " + itemId);
        }
        imageRepo.clearOtherPrimary(itemId, imageId);
        img.setIsPrimary(true);
        imageRepo.save(img);
    }

    @Override
    public void reorderImages(Long itemId, List<PortfolioImageOrderUpdateDTO> orders) {
        if (!repo.existsById(itemId)) {
            throw new NotFoundException("PortfolioItem " + itemId + " not found");
        }
        for (PortfolioImageOrderUpdateDTO o : orders) {
            PortfolioItemImage img = imageRepo.findById(o.getImageId())
                    .orElseThrow(() -> new NotFoundException("Image " + o.getImageId() + " not found"));
            if (!img.getItem().getId().equals(itemId)) {
                throw new NotFoundException("Image " + o.getImageId() + " does not belong to item " + itemId);
            }
            img.setSortOrder(o.getSortOrder());
            imageRepo.save(img);
        }
    }

    /* ----------------- 上/下架 ----------------- */

    @Override
    public void setPublished(Long itemId, boolean published) {
        PortfolioItem item = repo.findById(itemId)
                .orElseThrow(() -> new NotFoundException("PortfolioItem " + itemId + " not found"));
        item.setPublished(published);
        repo.save(item);
    }
}