package com.works.JessHsu.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.works.JessHsu.dto.PortfolioCardDto;
import com.works.JessHsu.dto.PortfolioItemDTO;
import com.works.JessHsu.dto.PortfolioItemDetailDTO;
import com.works.JessHsu.service.PortfolioItemService;

@RestController
@RequestMapping("/api/portfolioItems")
public class PortfolioItemController {

    private final PortfolioItemService service;

    public PortfolioItemController(PortfolioItemService service) {
        this.service = service;
    }

    /* ----------- Cards (前台卡片牆) ----------- */
    @GetMapping("/cards")
public Page<PortfolioCardDto> listCards(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "12") int size,
        @RequestParam(required = false) Boolean onlyPublished,
        @RequestParam(required = false) String category,
        @RequestParam(required = false) Long themeId
) {
    int p = Math.max(0, page);
    int sz = Math.max(1, size);

    // native query 內建 ORDER BY，不另外塞 Sort
    Pageable pageable = PageRequest.of(p, sz, Sort.unsorted());

    return service.listCards(pageable, onlyPublished, category, themeId);
}

    /* ----------- Detail (作品詳細頁) ----------- */
    @GetMapping("/{id}/detail")
    public PortfolioItemDetailDTO getDetail(@PathVariable Long id) {
        return service.getDetail(id);
    }

    /* ----------- 一般清單（前台讀取）----------- */
    @GetMapping
    public Page<PortfolioItemDTO> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sort, // e.g. "displayOrder,desc"
            @RequestParam(required = false) Boolean onlyPublished,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Long themeId,
            @RequestParam(required = false) String q) {
        int p = Math.max(0, page);
        int sz = Math.max(1, size);

        Sort s = resolveSort(sort); // 你原本的排序解析 helper
        PageRequest pr = PageRequest.of(p, sz, s);

        return service.list(
                pr,
                onlyPublished,
                category,
                themeId,
                q);
    }

    /** 安全的排序解析（白名單＋穩定次要排序） */
    private Sort resolveSort(String sort) {
        // 預設：顯示序 DESC，再用建立時間 DESC 穩定排序
        Sort fallback = Sort.by(
                Sort.Order.desc("displayOrder"),
                Sort.Order.desc("createdAt"));
        if (sort == null || sort.isBlank())
            return fallback;

        String[] parts = sort.split(",");
        String prop = parts[0].trim();
        String dir = (parts.length > 1 ? parts[1].trim().toLowerCase() : "desc");

        // 允許欄位白名單，避免注入
        java.util.Set<String> allow = java.util.Set.of("displayOrder", "createdAt", "id", "title", "category");
        if (!allow.contains(prop))
            return fallback;

        Sort primary = "asc".equals(dir) ? Sort.by(prop).ascending() : Sort.by(prop).descending();

        // 為避免相等值的不穩定順序，加上次要排序
        if (!"createdAt".equals(prop)) {
            primary = primary.and(Sort.by("createdAt").descending());
        }
        return primary;
    }
}