package com.works.JessHsu.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import com.works.JessHsu.dto.PortfolioCardDto;
import com.works.JessHsu.dto.PortfolioItemCreateDTO;
import com.works.JessHsu.dto.PortfolioItemDTO;
import com.works.JessHsu.dto.PortfolioItemDetailDTO;
import com.works.JessHsu.service.PortfolioItemService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/portfolioItems")
public class PortfolioItemController {

    private final PortfolioItemService service;

    public class PortfolioItemController {
        this.service = service;
    }

    @PostMapping
    public PortfolioItemDTO create(@Valid @RequestBody PortfolioItemCreateDTO dto) {
        return service.create(dto);

public class PortfolioItemController {
        return service.get(id);
    }

    /* ----------------- Cards (前台卡片牆) ----------------- */
    @GetMapping("/cards")
    public Page<PortfolioCardDto> listCards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) Boolean onlyPublished,
            @RequestParam(required = false) String category) {

        int p = Math.max(0, page);
        int sz = Math.max(1, size);

        // 不帶 Sort，避免 SQL 錯誤
        Pageable pageable = PageRequest.of(p, sz, Sort.unsorted());
        return service.listCards(pageable, onlyPublished, category);
    }

    /* ----------------- Detail (作品詳細頁) ----------------- */
    @GetMapping("/{id}/detail")
    public PortfolioItemDetailDTO getDetail(@PathVariable Long id) {
        return service.getDetail(id);
    }

    /* ----------------- 一般清單 ----------------- */
    @GetMapping
    public Page<PortfolioItemDTO> list(
            @RequestParam(defaultValue = "0") int page,

public class PortfolioItemController {
            @RequestParam(required = false) Boolean onlyPublished,
            @RequestParam(required = false) String category) {
int p = Math.max(0, page);
        int sz = Math.max(1, size);

        String[] parts = sort.split(",");
        Sort by;
        if (parts.length >= 2) {
            Sort.Direction dir;
            try {
                dir = Sort.Direction.fromString(parts[1].trim());
            } catch (IllegalArgumentException ex) {
                dir = Sort.Direction.DESC;
            }
            by = Sort.by(dir, parts[0].trim());
        } else {
            by = Sort.by(sort.trim());
        }

        return service.list(PageRequest.of(p, sz, by), onlyPublished, category);
    }
    