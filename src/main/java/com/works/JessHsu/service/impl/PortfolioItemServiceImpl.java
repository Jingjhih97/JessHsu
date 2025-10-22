package com.works.JessHsu.service.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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

    private static final int MAX_IMAGES = 5;

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

        // ⭐ 新增：displayOrder = 目前最大值 + 1（新作在最前）
        Integer maxOrder = repo.findMaxDisplayOrder();
        int nextOrder = (maxOrder == null ? 0 : maxOrder) + 1;
        entity.setDisplayOrder(nextOrder);

        PortfolioItem saved = repo.save(entity);

        // 一次把封面與圖片清單寫入 images，並處理主圖與上限
        upsertImagesFromDtos(saved.getId(), dto.getCoverImageUrl(), dto.getImages());

        return PortfolioItemMapper.toDTO(saved);
    }

    @Override
    public PortfolioItemDTO update(Long id, PortfolioItemCreateDTO dto) {
        PortfolioItem e = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("PortfolioItem " + id + " not found"));

        // 不動 displayOrder 的情況下更新其它欄位
        PortfolioItemMapper.updateEntity(e, dto);
        PortfolioItem saved = repo.save(e);

        // 若有帶封面或圖片清單就同步（不刪舊圖）
        if (StringUtils.hasText(dto.getCoverImageUrl())
                || (dto.getImages() != null && !dto.getImages().isEmpty())) {
            upsertImagesFromDtos(saved.getId(), dto.getCoverImageUrl(), dto.getImages());
        }

        return PortfolioItemMapper.toDTO(saved);
    }

    @Override
    public void delete(Long id) {
        PortfolioItem target = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("PortfolioItem " + id + " not found"));

        // 先記住被刪那筆的 displayOrder
        int removedOrder = target.getDisplayOrder() == null ? 0 : target.getDisplayOrder();

        // 刪除該筆
        repo.delete(target);

        // ⭐ 向前遞補：把所有 displayOrder > removedOrder 的通通 -1
        repo.compactDisplayOrderAfter(removedOrder);
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
        // ⭐ 改用自訂查詢，確保以 displayOrder DESC, createdAt DESC 排序
        return repo.search(onlyPublished, category, pageable).map(PortfolioItemMapper::toDTO);
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
        if (!repo.existsById(itemId)) {
            throw new NotFoundException("PortfolioItem " + itemId + " not found");
        }
        PortfolioItem itemRef = repo.getReferenceById(itemId);

        PortfolioItemImage img = new PortfolioItemImage();
        img.setItem(itemRef);
        img.setImageUrl(dto.getImageUrl());

        Integer maxSort = imageRepo.findMaxSort(itemId);
        int nextSort = (maxSort == null ? -1 : maxSort) + 1;
        img.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : nextSort);

        boolean wantPrimary = Boolean.TRUE.equals(dto.getIsPrimary());
        img.setIsPrimary(wantPrimary);

        img = imageRepo.save(img);

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
                .orElseThrow(() -> new NotFoundException("Image not found"));

        if (!img.getItem().getId().equals(itemId)) {
            throw new NotFoundException("Image does not belong to this item");
        }

        // ✅ 1. 清掉其他主圖
        imageRepo.clearOtherPrimary(itemId, imageId);

        // ✅ 2. 設定目前為主圖
        if (!Boolean.TRUE.equals(img.getIsPrimary())) {
            img.setIsPrimary(true);
            imageRepo.save(img);
        }

        // ✅ 3. 同步更新 portfolio_items.cover_image_url
        PortfolioItem item = img.getItem();
        if (item != null) {
            item.setCoverImageUrl(img.getImageUrl());
            repo.save(item);
        }
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

    /* ====================== 私有：封面 + 多圖 同步（一次建立/更新） ====================== */

    /**
     * 將 cover + images(dto) 同步到 portfolio_item_images：
     * - 以輸入順序為準（封面優先）
     * - 去空白、去重複 URL
     * - 總數上限 5（含封面）
     * - 若有多個 isPrimary=true，以第一個為準；若都沒有，封面為主圖；再沒有就第一張
     * - 只新增缺少的圖片，不刪除舊圖片
     */
    private void upsertImagesFromDtos(Long itemId, String coverUrl, List<PortfolioImageCreateDTO> imagesDto) {
        if (itemId == null)
            return;

        // 1) 蒐集欲呈現的 URL（保序去重）
        Map<String, Boolean> wanted = new LinkedHashMap<>();

        // 先放封面（封面優先）
        String cover = StringUtils.hasText(coverUrl) ? coverUrl.trim() : null;
        if (cover != null && !cover.isEmpty()) {
            wanted.put(cover, true); // 預設封面想要主圖（若之後有明確 isPrimary 會覆蓋）
        }

        // 再放圖片 DTO（尊重 isPrimary）
        if (imagesDto != null) {
            for (PortfolioImageCreateDTO d : imagesDto) {
                if (d == null || !StringUtils.hasText(d.getImageUrl()))
                    continue;
                String u = d.getImageUrl().trim();
                boolean primary = Boolean.TRUE.equals(d.getIsPrimary());
                wanted.merge(u, primary, (oldV, newV) -> oldV || newV);
            }
        }

        if (wanted.isEmpty())
            return;

        // 限制最多 5 張（保序）
        if (wanted.size() > MAX_IMAGES) {
            Set<String> limited = new LinkedHashSet<>();
            int i = 0;
            for (String u : wanted.keySet()) {
                if (i++ >= MAX_IMAGES)
                    break;
                limited.add(u);
            }
            wanted.keySet().retainAll(limited);
        }

        // 2) 讀取現有圖片與 URL 集合
        List<PortfolioItemImage> existing = imageRepo.findByItem_IdOrderByIsPrimaryDescSortOrderAscIdAsc(itemId);
        Set<String> existingUrls = existing.stream()
                .map(PortfolioItemImage::getImageUrl)
                .filter(Objects::nonNull)
                .map(String::trim)
                .collect(Collectors.toSet());

        // 3) 依序補新增不存在的圖片（新圖先 isPrimary=false，最後再統一挑主圖）
        Integer maxSort = imageRepo.findMaxSort(itemId);
        int nextSort = (maxSort == null ? -1 : maxSort) + 1;

        PortfolioItem itemRef = repo.getReferenceById(itemId);
        List<PortfolioItemImage> union = new ArrayList<>(existing); // union = 現有 + 本次新增

        for (String url : wanted.keySet()) {
            if (!existingUrls.contains(url)) {
                PortfolioItemImage img = new PortfolioItemImage();
                img.setItem(itemRef);
                img.setImageUrl(url);
                img.setSortOrder(nextSort++);
                img.setIsPrimary(false); // 主圖稍後統一處理
                img = imageRepo.save(img);
                union.add(img);
                existingUrls.add(url);
            }
        }

        // 4) 決定主圖
        String candidateFromDto = null;
        if (imagesDto != null) {
            for (PortfolioImageCreateDTO d : imagesDto) {
                if (d != null && Boolean.TRUE.equals(d.getIsPrimary()) && StringUtils.hasText(d.getImageUrl())) {
                    String trimmed = d.getImageUrl().trim();
                    if (wanted.containsKey(trimmed)) {
                        candidateFromDto = trimmed;
                        break;
                    }
                }
            }
        }
        boolean coverIsCandidate = (cover != null && wanted.containsKey(cover));
        String firstWanted = wanted.keySet().iterator().next();

        final String primaryUrl = (candidateFromDto != null)
                ? candidateFromDto
                : (coverIsCandidate ? cover : firstWanted);

        PortfolioItemImage primaryImg = union.stream()
                .filter(i -> Objects.equals(i.getImageUrl(), primaryUrl))
                .findFirst()
                .orElse(null);

        if (primaryImg != null) {
            imageRepo.clearOtherPrimary(itemId, primaryImg.getId());
            if (!Boolean.TRUE.equals(primaryImg.getIsPrimary())) {
                primaryImg.setIsPrimary(true);
                imageRepo.save(primaryImg);
            }
        }
    }
}