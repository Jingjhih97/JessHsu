package com.works.JessHsu.controller.admin;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.works.JessHsu.dto.ThemeDTO;
import com.works.JessHsu.dto.ThemeUpdateDTO;
import com.works.JessHsu.entity.Theme;
import com.works.JessHsu.exception.NotFoundException;
import com.works.JessHsu.repository.PortfolioItemRepository;
import com.works.JessHsu.repository.ThemeRepository;

@RestController
@RequestMapping("/api/admin/themes")
public class AdminThemeController {

    private final ThemeRepository themeRepo;
    private final PortfolioItemRepository itemRepo;

    public AdminThemeController(ThemeRepository themeRepo,
                                PortfolioItemRepository itemRepo) {
        this.themeRepo = themeRepo;
        this.itemRepo = itemRepo;
    }

    // GET /api/admin/themes
    @GetMapping
    public List<ThemeDTO> listAll() {
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

    // POST /api/admin/themes
    @PostMapping
    public ThemeDTO create(@RequestBody ThemeDTO body) {
        String name = trimToNull(body.getName());
        if (name == null) {
            throw new IllegalArgumentException("Theme name is required");
        }
        if (themeRepo.existsByName(name)) {
            throw new IllegalStateException("Theme name already exists");
        }

        Theme t = new Theme();
        t.setName(name);
        t.setImageUrl(trimToNull(body.getImageUrl()));
        t.setDescription(trimToNull(body.getDescription()));

        Theme saved = themeRepo.save(t);

        ThemeDTO dto = new ThemeDTO();
        dto.setId(saved.getId());
        dto.setName(saved.getName());
        dto.setCreatedAt(saved.getCreatedAt());
        dto.setUsageCount(0L);
        dto.setImageUrl(saved.getImageUrl());
        dto.setDescription(saved.getDescription());
        return dto;
    }

    // DELETE /api/admin/themes/{id}
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        Theme t = themeRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Theme " + id + " not found"));

        long usageCount = itemRepo.countByTheme_Id(id);
        if (usageCount > 0) {
            // 也可以改成回 409 Conflict 的全域例外處理
            throw new IllegalStateException("This theme is still used by some portfolio items");
        }

        themeRepo.delete(t);
    }

    // PUT /api/admin/themes/{id}
    @PutMapping("/{id}")
    public ThemeDTO update(@PathVariable Long id, @RequestBody ThemeUpdateDTO body) {
        Theme t = themeRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Theme " + id + " not found"));

        // name（可選）
        if (body.getName() != null) {
            String newName = trimToNull(body.getName());
            if (newName == null) {
                // 若前端明確傳入空字串，視為要清空 -> 不允許，避免出現無名主題
                throw new IllegalArgumentException("Theme name cannot be empty");
            }
            if (!newName.equalsIgnoreCase(t.getName()) && themeRepo.existsByName(newName)) {
                throw new IllegalStateException("Theme name already exists");
            }
            t.setName(newName);
        }

        // imageUrl（可選，允許清空）
        if (body.getImageUrl() != null) {
            t.setImageUrl(trimToNull(body.getImageUrl()));
        }

        // ✅ description（可選，允許清空）
        if (body.getDescription() != null) {
            t.setDescription(trimToNull(body.getDescription()));
        }

        Theme saved = themeRepo.save(t);
        long usage = itemRepo.countByTheme_Id(saved.getId());

        ThemeDTO dto = new ThemeDTO();
        dto.setId(saved.getId());
        dto.setName(saved.getName());
        dto.setCreatedAt(saved.getCreatedAt());
        dto.setUsageCount(usage);
        dto.setImageUrl(saved.getImageUrl());
        dto.setDescription(saved.getDescription()); // ✅ 帶回描述
        return dto;
    }

    // ===== helpers =====
    private static String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}