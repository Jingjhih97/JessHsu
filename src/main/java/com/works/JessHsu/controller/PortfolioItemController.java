// src/main/java/com/works/JessHsu/controller/PortfolioItemController.java
package com.works.JessHsu.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping; // ★ 新增：作品詳細 DTO
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    public PortfolioItemController(PortfolioItemService service) {
        this.service = service;
    }

    /* ----------------- CRUD ----------------- */

    @PostMapping
    public PortfolioItemDTO create(@Valid @RequestBody PortfolioItemCreateDTO dto) {
        return service.create(dto);
    }

    @PutMapping("/{id}")
    public PortfolioItemDTO update(@PathVariable Long id, @Valid @RequestBody PortfolioItemCreateDTO dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @GetMapping("/{id}")
    public PortfolioItemDTO get(@PathVariable Long id) {
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
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort,
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
}