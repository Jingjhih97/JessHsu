package com.works.JessHsu.service.impl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.works.JessHsu.dto.PortfolioCardDTO;
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
import com.works.JessHsu.service.PortfolioItemService;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class PortfolioItemServiceImpl implements PortfolioItemService {

    private final PortfolioItemRepository itemRepo;
    private final PortfolioItemImageRepository imageRepo;

    /* ----------------- 既有 CRUD ----------------- */

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
        if (!itemRepo.existsById(id)) {
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
        return itemRepo.search(onlyPublished, category, pageable)
                .map(PortfolioItemMapper::toDTO);
    }

    /* ----------------- 卡片清單（每作品 1 張主圖） ----------------- */

    @Override
    @Transactional(readOnly = true)
    public Page<PortfolioCardDTO> listCards(Pageable pageable, Boolean onlyPublished, String category) {
        // Repository 的 searchCards_query 已經包含從 images 表選主圖的邏輯
        return itemRepo.searchCards(onlyPublished, category, pageable);
    }

    /* ----------------- 圖片管理 ----------------- */

    @Override
    @Transactional(readOnly = true)
    public List<PortfolioImageDTO> listImages(Long itemId) {
        ensureItem(itemId);
        return imageRepo.findByItemIdOrderBySortOrderAscIdAsc(itemId)
                .stream()
                .map(this::toImageDTO)
                .toList();
    }

    @Override
    public PortfolioImageDTO addImage(Long itemId, PortfolioImageCreateDTO dto) {
        PortfolioItem item = itemRepo.findById(itemId)
                .orElseThrow(() -> new NotFoundException("PortfolioItem " + itemId + " not found"));

        PortfolioItemImage img = new PortfolioItemImage();
        img.setItem(item);
        img.setImageUrl(dto.imageUrl());

        // sortOrder：若外部沒給就接在最後
        Integer maxSort = imageRepo.findMaxSort(itemId);
        int nextSort = (maxSort == null ? -1 : maxSort) + 1;
        img.setSortOrder(dto.sortOrder() != null ? dto.sortOrder() : nextSort);

        boolean wantPrimary = dto.isPrimary() != null && dto.isPrimary();
        img.setIsPrimary(wantPrimary);

        img = imageRepo.save(img);

        // 若設為主圖，清掉該 item 其他的主圖
        if (wantPrimary) {
            imageRepo.clearOtherPrimary(itemId, img.getId());
        }

        return toImageDTO(img);
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
        ensureItem(itemId);
        for (PortfolioImageOrderUpdateDTO o : orders) {
            PortfolioItemImage img = imageRepo.findById(o.imageId())
                    .orElseThrow(() -> new NotFoundException("Image " + o.imageId() + " not found"));
            if (!img.getItem().getId().equals(itemId)) {
                throw new NotFoundException("Image " + o.imageId() + " does not belong to item " + itemId);
            }
            img.setSortOrder(o.sortOrder());
            imageRepo.save(img);
        }
    }

    @Override
    public void setPublished(Long itemId, boolean published) {
        PortfolioItem item = itemRepo.findById(itemId)
                .orElseThrow(() -> new NotFoundException("PortfolioItem " + itemId + " not found"));
        item.setPublished(published);
        itemRepo.save(item);
    }

    @Override
    @Transactional(readOnly = true)
    public PortfolioItemDetailDTO getDetail(Long id) {
        // 用自訂的 repository 方法一次抓取 item + images
        PortfolioItem item = itemRepo.findByIdWithImages(id)
                .orElseThrow(() -> new NotFoundException("PortfolioItem " + id + " not found"));

        // 轉成 DTO
        List<PortfolioImageDTO> imageDTOs = item.getImages()
                .stream()
                .map(this::toImageDTO)
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

    /* ----------------- 私有 method & helpers ----------------- */

    private void ensureItem(Long itemId) {
        if (!itemRepo.existsById(itemId)) {
            throw new NotFoundException("PortfolioItem " + itemId + " not found");
        }
    }

    private PortfolioImageDTO toImageDTO(PortfolioItemImage i) {
        return new PortfolioImageDTO(
                i.getId(),
                i.getImageUrl(),
                Boolean.TRUE.equals(i.getIsPrimary()),
                i.getSortOrder());
    }
}