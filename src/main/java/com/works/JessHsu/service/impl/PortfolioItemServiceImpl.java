// src/main/java/com/works/JessHsu/service/impl/PortfolioItemServiceImpl.java
package com.works.JessHsu.service.impl;

import java.util.Comparator;
import java.util.HashSet;
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

import com.works.JessHsu.dto.ImageUpsertDTO;
import com.works.JessHsu.dto.PortfolioCardDto;
import com.works.JessHsu.dto.PortfolioImageCreateDTO;
import com.works.JessHsu.dto.PortfolioImageCropDTO;
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
import com.works.JessHsu.repository.ThemeRepository;
import com.works.JessHsu.repository.view.PortfolioCardView;
import com.works.JessHsu.service.PortfolioItemService;

@Service
@Transactional
public class PortfolioItemServiceImpl implements PortfolioItemService {

    private static final int MAX_IMAGES = 5;

    private final PortfolioItemRepository repo;
    private final PortfolioItemImageRepository imageRepo;
    private final ThemeRepository themeRepo;

    public PortfolioItemServiceImpl(
            PortfolioItemRepository repo,
            PortfolioItemImageRepository imageRepo,
            ThemeRepository themeRepo) {
        this.repo = repo;
        this.imageRepo = imageRepo;
        this.themeRepo = themeRepo;
    }

    /* ==================== 封面規則 / 同步 ==================== */

    /** 依規則重算封面並同步到 item.coverImageUrl / coverItemImage */
    private void recomputeCover(Long itemId) {
        PortfolioItem item = repo.findById(itemId)
                .orElseThrow(() -> new NotFoundException("PortfolioItem " + itemId + " not found"));

        List<PortfolioItemImage> images = imageRepo.findByItem_IdOrderByIsPrimaryDescSortOrderAscIdAsc(itemId);

        PortfolioItemImage cover = images.stream()
                .filter(i -> Boolean.TRUE.equals(i.getIsPrimary()))
                .findFirst()
                .orElseGet(() -> images.isEmpty() ? null : images.get(0));

        item.setCoverImageUrl(cover != null ? cover.getImageUrl() : null);

        // 若你的實體有 coverItemImage 欄位就同步；沒有就忽略
        try {
            item.getClass().getMethod("setCoverItemImage", PortfolioItemImage.class);
            item.setCoverItemImage(cover);
        } catch (NoSuchMethodException ignore) {
        }

        repo.save(item);
    }

    /* =========================== CRUD =========================== */

    @Override
    public PortfolioItemDTO create(PortfolioItemCreateDTO dto) {
        PortfolioItem entity = PortfolioItemMapper.toEntity(dto);

        if (dto.getThemeId() != null) {
            var theme = themeRepo.findById(dto.getThemeId())
                    .orElseThrow(() -> new NotFoundException("Theme " + dto.getThemeId() + " not found"));
            entity.setTheme(theme);
        } else {
            entity.setTheme(null);
        }

        // 排序邏輯照舊
        Integer maxOrder = repo.findMaxDisplayOrder();
        int nextOrder = (maxOrder == null ? 0 : maxOrder) + 1;
        entity.setDisplayOrder(nextOrder);

        PortfolioItem saved = repo.save(entity);

        // 圖片 + 封面
        upsertImagesFromDtos(saved.getId(), dto.getCoverImageUrl(), dto.getImages());
        recomputeCover(saved.getId());

        return PortfolioItemMapper.toDTO(saved);
    }

    @Override
    public PortfolioItemDTO update(Long id, PortfolioItemCreateDTO dto) {
        PortfolioItem e = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("PortfolioItem " + id + " not found"));

        // 先更新基本欄位
        PortfolioItemMapper.updateEntity(e, dto);

        // 🔥 更新 theme
        if (dto.getThemeId() != null) {
            var theme = themeRepo.findById(dto.getThemeId())
                    .orElseThrow(() -> new NotFoundException("Theme " + dto.getThemeId() + " not found"));
            e.setTheme(theme);
        } else {
            e.setTheme(null);
        }

        PortfolioItem saved = repo.save(e);

        // 如果有傳 images，就同步圖片
        boolean touchedImages = (dto.getImages() != null && !dto.getImages().isEmpty());
        if (touchedImages) {
            upsertImagesFromDtos(saved.getId(), null /* 不再用 cover 決主圖 */, dto.getImages());
            recomputeCover(saved.getId());
        }

        return PortfolioItemMapper.toDTO(saved);
    }

    @Override
    public void delete(Long id) {
        PortfolioItem target = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("PortfolioItem " + id + " not found"));
        int removedOrder = target.getDisplayOrder() == null ? 0 : target.getDisplayOrder();
        repo.delete(target);
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
    public Page<PortfolioItemDTO> list(
            Pageable pageable,
            Boolean onlyPublished,
            String category,
            Long themeId,
            String q) {
        // 正規化關鍵字：null 或空字串都當成不過濾
        String qq = (q == null) ? null : q.trim();
        if (qq != null && qq.isEmpty()) {
            qq = null;
        }

        Page<PortfolioItem> page = repo.search(
                onlyPublished,
                category,
                qq,
                themeId,
                pageable);

        return page.map(PortfolioItemMapper::toDTO);
    }

    /* ======================= Cards (前台) ======================= */

    @Override
    @Transactional(readOnly = true)
    public Page<PortfolioCardDto> listCards(
            Pageable pageable,
            Boolean onlyPublished,
            String category,
            Long themeId) {
        Page<PortfolioCardView> page = repo.searchCardsNative(
                onlyPublished,
                category,
                themeId,
                pageable);

        return page.map(v -> new PortfolioCardDto(
                v.getId(),
                v.getTitle(),
                v.getCategory(),
                v.getCoverImageUrl(),
                v.getCreatedAt() == null ? null : v.getCreatedAt().toLocalDateTime()));
    }

    /* ==================== Detail (前台詳細) ===================== */

    @Override
    @Transactional(readOnly = true)
    public PortfolioItemDetailDTO getDetail(Long id) {
        PortfolioItem item = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("PortfolioItem " + id + " not found"));

        var images = imageRepo.findByItem_IdOrderByIsPrimaryDescSortOrderAscIdAsc(id);

        List<PortfolioImageDTO> imageDTOs = images.stream()
                .map(i -> {
                    PortfolioImageDTO d = new PortfolioImageDTO(
                            i.getId(),
                            item.getId(),
                            i.getImageUrl(),
                            Boolean.TRUE.equals(i.getIsPrimary()),
                            i.getSortOrder(),
                            i.getCreatedAt());
                    // 如果你前端裁切預覽要用 cropX/cropY/cropSize，也可以在這裡塞
                    d.setCropX(i.getCropX());
                    d.setCropY(i.getCropY());
                    d.setCropSize(i.getCropSize());
                    return d;
                })
                .toList();

        PortfolioItemDetailDTO dto = new PortfolioItemDetailDTO(
                item.getId(),
                item.getTitle(),
                item.getDescription(),
                item.getCategory(),
                item.getPublished(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                imageDTOs,
                item.getTheme() != null ? item.getTheme().getId() : null);

        dto.setThemeId(item.getTheme() != null ? item.getTheme().getId() : null);

        return dto;
    } /* ===================== 圖片管理（後台） ===================== */

    @Override
    @Transactional(readOnly = true)
    public List<PortfolioImageDTO> listImages(Long itemId) {
        if (!repo.existsById(itemId))
            throw new NotFoundException("PortfolioItem " + itemId + " not found");
        return imageRepo.findByItem_IdOrderByIsPrimaryDescSortOrderAscIdAsc(itemId)
                .stream()
                .map(i -> {
                    PortfolioImageDTO d = new PortfolioImageDTO();
                    d.setId(i.getId());
                    d.setItemId(itemId);
                    d.setImageUrl(i.getImageUrl());
                    d.setIsPrimary(Boolean.TRUE.equals(i.getIsPrimary()));
                    d.setSortOrder(i.getSortOrder());
                    d.setCreatedAt(i.getCreatedAt());
                    // ✅ 補上裁剪
                    d.setCropX(i.getCropX());
                    d.setCropY(i.getCropY());
                    d.setCropSize(i.getCropSize());
                    return d;
                }).toList();
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

        // 新增/變更後統一重算封面
        recomputeCover(itemId);

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

        // 先處理可能的外鍵引用（封面）
        PortfolioItem item = img.getItem();
        boolean isCoverLinked = false;
        try {
            // 如果你的 Entity 有 coverItemImage 欄位，且指向這張圖
            if (item.getCoverItemImage() != null && Objects.equals(item.getCoverItemImage().getId(), imageId)) {
                item.setCoverItemImage(null);
                isCoverLinked = true;
            }
        } catch (Exception ignore) {
            /* 專案沒有 coverItemImage 欄位就略過 */ }

        // 如果 cover_image_url 也是這張圖的 URL，一併先清空
        if (Objects.equals(item.getCoverImageUrl(), img.getImageUrl())) {
            item.setCoverImageUrl(null);
            isCoverLinked = true;
        }

        if (isCoverLinked) {
            repo.save(item); // 先存一版，解除外鍵/引用
        }

        // 再刪圖片
        imageRepo.delete(img);

        // 最後重算封面（會選主圖或第一張填回 cover）
        recomputeCover(itemId);
    }

    @Override
    public void setPrimaryImage(Long itemId, Long imageId) {
        PortfolioItemImage img = imageRepo.findById(imageId)
                .orElseThrow(() -> new NotFoundException("Image not found"));

        if (!img.getItem().getId().equals(itemId)) {
            throw new NotFoundException("Image does not belong to this item");
        }

        imageRepo.clearOtherPrimary(itemId, imageId);
        if (!Boolean.TRUE.equals(img.getIsPrimary())) {
            img.setIsPrimary(true);
            imageRepo.save(img);
        }

        recomputeCover(itemId);
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
        // 若你希望「第一張就是封面」，在此可呼叫 recomputeCover(itemId)
    }

    /* ======================== 上 / 下架 ======================== */

    @Override
    public void setPublished(Long itemId, boolean published) {
        PortfolioItem item = repo.findById(itemId)
                .orElseThrow(() -> new NotFoundException("PortfolioItem " + itemId + " not found"));
        item.setPublished(published);
        repo.save(item);
    }

    /* ========== 多圖同步（建立/更新時）＋最後統一重算封面 ========== */

    private void upsertImagesFromDtos(Long itemId, String coverUrl, List<PortfolioImageCreateDTO> imagesDto) {
        if (itemId == null)
            return;

        Map<String, Boolean> wanted = new LinkedHashMap<>();

        String cover = StringUtils.hasText(coverUrl) ? coverUrl.trim() : null;
        if (cover != null && !cover.isEmpty()) {
            wanted.put(cover, true);
        }

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

        List<PortfolioItemImage> existing = imageRepo.findByItem_IdOrderByIsPrimaryDescSortOrderAscIdAsc(itemId);
        Set<String> existingUrls = existing.stream()
                .map(PortfolioItemImage::getImageUrl)
                .filter(Objects::nonNull)
                .map(String::trim)
                .collect(Collectors.toSet());

        Integer maxSort = imageRepo.findMaxSort(itemId);
        int nextSort = (maxSort == null ? -1 : maxSort) + 1;

        PortfolioItem itemRef = repo.getReferenceById(itemId);

        for (String url : wanted.keySet()) {
            if (!existingUrls.contains(url)) {
                PortfolioItemImage img = new PortfolioItemImage();
                img.setItem(itemRef);
                img.setImageUrl(url);
                img.setSortOrder(nextSort++);
                img.setIsPrimary(false);
                imageRepo.save(img);
                existingUrls.add(url);
            }
        }

        // 指定主圖（DTO 中第一個 isPrimary=true > 封面 > 第一張）
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

        PortfolioItemImage primaryImg = imageRepo.findByItem_IdOrderByIsPrimaryDescSortOrderAscIdAsc(itemId)
                .stream().filter(i -> Objects.equals(i.getImageUrl(), primaryUrl))
                .findFirst().orElse(null);

        if (primaryImg != null) {
            imageRepo.clearOtherPrimary(itemId, primaryImg.getId());
            if (!Boolean.TRUE.equals(primaryImg.getIsPrimary())) {
                primaryImg.setIsPrimary(true);
                imageRepo.save(primaryImg);
            }
        }

        // 最後重算封面
        recomputeCover(itemId);
    }

    /* =================== 裁切覆蓋：更新圖片 URL =================== */

    @Override
    public void updateImageUrl(Long itemId, Long imageId, String newUrl) {
        var img = imageRepo.findById(imageId)
                .orElseThrow(() -> new NotFoundException("Image not found"));
        if (!img.getItem().getId().equals(itemId)) {
            throw new NotFoundException("Image not belongs to item");
        }

        String oldUrl = img.getImageUrl(); // 先記下舊路徑
        img.setImageUrl(newUrl);
        imageRepo.save(img);

        // 若是主圖，同步 cover
        if (Boolean.TRUE.equals(img.getIsPrimary())) {
            recomputeCover(itemId);
        }

        // ★★ 刪除舊檔（只刪本機 /uploads/ 開頭的；http/https 不處理）
        try {
            if (oldUrl != null && oldUrl.startsWith("/uploads/")) {
                java.nio.file.Path p = java.nio.file.Paths.get("uploads", oldUrl.substring("/uploads/".length()));
                java.nio.file.Files.deleteIfExists(p);
            }
        } catch (Exception ex) {
        }
    }

    @Override
    public void updateImageCrop(Long itemId, Long imageId, PortfolioImageCropDTO crop) {
        var img = imageRepo.findById(imageId)
                .orElseThrow(() -> new NotFoundException("Image not found"));
        if (!img.getItem().getId().equals(itemId)) {
            throw new NotFoundException("Image not belongs to item");
        }
        img.setCropX(crop.getX());
        img.setCropY(crop.getY());
        img.setCropSize(crop.getSize());
        imageRepo.save(img);
        // 不需要重算封面，因為 URL 沒變；若希望列表縮圖立刻反映裁切，前端會用 crop 參數渲染
    }

    @Override
    public void replaceImages(Long itemId, List<ImageUpsertDTO> imagesDto) {
        PortfolioItem item = repo.findById(itemId)
                .orElseThrow(() -> new NotFoundException("PortfolioItem " + itemId + " not found"));

        // 既有圖片 map
        List<PortfolioItemImage> existing = imageRepo.findByItem_IdOrderByIsPrimaryDescSortOrderAscIdAsc(itemId);
        Map<Long, PortfolioItemImage> byId = existing.stream()
                .filter(e -> e.getId() != null)
                .collect(Collectors.toMap(PortfolioItemImage::getId, e -> e));

        // 正常化排序：如果有 null sortOrder，先補 0..n-1
        if (imagesDto != null) {
            int idx = 0;
            for (ImageUpsertDTO d : imagesDto) {
                if (d.getSortOrder() == null)
                    d.setSortOrder(idx++);
            }
            imagesDto.sort(Comparator.comparing(ImageUpsertDTO::getSortOrder, Comparator.nullsLast(Integer::compareTo))
                    .thenComparing(d -> d.getId() == null ? Long.MAX_VALUE : d.getId()));
        } else {
            imagesDto = List.of();
        }

        // 先標記想保留的 id
        Set<Long> keepIds = new HashSet<>();
        int nextSort = 0;

        // 更新 & 新增
        for (ImageUpsertDTO d : imagesDto) {
            if (d.getId() != null && byId.containsKey(d.getId())) {
                // 更新
                PortfolioItemImage img = byId.get(d.getId());
                img.setImageUrl(d.getImageUrl());
                img.setIsPrimary(Boolean.TRUE.equals(d.getIsPrimary()));
                img.setSortOrder(d.getSortOrder() == null ? nextSort++ : d.getSortOrder());
                img.setCropX(d.getCropX());
                img.setCropY(d.getCropY());
                img.setCropSize(d.getCropSize());
                imageRepo.save(img);
                keepIds.add(img.getId());
            } else {
                // 新增
                PortfolioItemImage img = new PortfolioItemImage();
                img.setItem(item);
                img.setImageUrl(d.getImageUrl());
                img.setIsPrimary(Boolean.TRUE.equals(d.getIsPrimary()));
                img.setSortOrder(d.getSortOrder() == null ? nextSort++ : d.getSortOrder());
                img.setCropX(d.getCropX());
                img.setCropY(d.getCropY());
                img.setCropSize(d.getCropSize());
                imageRepo.save(img);
                keepIds.add(img.getId());
            }
        }

        // 刪除：不在 keepIds 的既有圖片
        for (PortfolioItemImage e : existing) {
            if (!keepIds.contains(e.getId())) {
                imageRepo.delete(e);
            }
        }

        // 主圖唯一性校正
        List<PortfolioItemImage> finalList = imageRepo.findByItem_IdOrderByIsPrimaryDescSortOrderAscIdAsc(itemId);
        PortfolioItemImage primary = null;
        for (PortfolioItemImage img : finalList) {
            if (Boolean.TRUE.equals(img.getIsPrimary())) {
                if (primary == null) {
                    primary = img;
                } else {
                    img.setIsPrimary(false);
                    imageRepo.save(img);
                }
            }
        }
        if (primary == null && !finalList.isEmpty()) {
            // 沒有主圖就把第一張當主圖
            PortfolioItemImage first = finalList.stream()
                    .sorted(Comparator.comparing(PortfolioItemImage::getSortOrder)
                            .thenComparing(PortfolioItemImage::getId))
                    .findFirst().orElse(null);
            if (first != null && !Boolean.TRUE.equals(first.getIsPrimary())) {
                first.setIsPrimary(true);
                imageRepo.save(first);
            }
        }

        // 同步封面
        recomputeCover(itemId);
    }
}