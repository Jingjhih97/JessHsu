package com.works.JessHsu.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.works.JessHsu.dto.ThemeDTO;
import com.works.JessHsu.entity.Theme;
import com.works.JessHsu.exception.NotFoundException;
import com.works.JessHsu.repository.PortfolioItemRepository;
import com.works.JessHsu.repository.ThemeRepository;

@RestController
@RequestMapping("/api/themes")
public class ThemeController {

    private final ThemeRepository themeRepo;
    private final PortfolioItemRepository itemRepo;

    public ThemeController(ThemeRepository themeRepo,
                           PortfolioItemRepository itemRepo) {
        this.themeRepo = themeRepo;
        this.itemRepo = itemRepo;
    }

    // GET /api/themes
    @GetMapping
    public List<ThemeDTO> listPublicThemes() {
        return themeRepo.findAllByOrderByNameAsc()
                .stream()
                .map(t -> {
                    long usage = itemRepo.countByTheme_Id(t.getId());

                    ThemeDTO dto = new ThemeDTO();
                    dto.setId(t.getId());
                    dto.setName(t.getName());
                    dto.setCreatedAt(t.getCreatedAt());
                    dto.setUsageCount(usage);
                    dto.setImageUrl(t.getImageUrl());
                    dto.setDescription(t.getDescription());
                    return dto;
                })
                .toList();
    }

     @GetMapping("/{id}")
    public ThemeDTO getOne(@PathVariable Long id) {
        Theme t = themeRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Theme not found"));
        return toDto(t);
    }

    private ThemeDTO toDto(Theme t) {
        ThemeDTO dto = new ThemeDTO();
        dto.setId(t.getId());
        dto.setName(t.getName());
        dto.setImageUrl(t.getImageUrl());
        dto.setDescription(t.getDescription());
        dto.setCreatedAt(t.getCreatedAt());
        return dto;
    }
}