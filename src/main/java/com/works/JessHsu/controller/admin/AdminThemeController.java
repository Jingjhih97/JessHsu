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
                    return dto;
                })
                .toList();
    }

    // POST /api/admin/themes
    @PostMapping
    public ThemeDTO create(@RequestBody ThemeDTO body) {
        String name = body.getName();
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Theme name is required");
        }

        if (themeRepo.existsByName(name.trim())) {
            throw new IllegalStateException("Theme name already exists");
        }

        Theme t = new Theme();
        t.setName(name.trim());
        t.setImageUrl(body.getImageUrl()); // <== 這行很重要，讓主題封面一起存
        // createdAt 如果在 Theme entity 上有 @PrePersist 自動塞，那就不用手動 set
        // 如果沒有自動欄位，請自己塞:
        // t.setCreatedAt(LocalDateTime.now());

        Theme saved = themeRepo.save(t);

        ThemeDTO dto = new ThemeDTO();
        dto.setId(saved.getId());
        dto.setName(saved.getName());
        dto.setCreatedAt(saved.getCreatedAt());
        dto.setUsageCount(0L);
        dto.setImageUrl(saved.getImageUrl());
        return dto;
    }

    // DELETE /api/admin/themes/{id}
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {

        // 1. 確認存在
        Theme t = themeRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Theme " + id + " not found"));

        // 2. 確認沒人用
        long usageCount = itemRepo.countByTheme_Id(id);
        if (usageCount > 0) {
            // 你也可以回傳 409 Conflict，這裡先丟 runtime exception
            throw new IllegalStateException("This theme is still used by some portfolio items");
        }

        // 3. 刪除
        themeRepo.delete(t);
    }

    @PutMapping("/{id}")
    public ThemeDTO update(
            @PathVariable Long id,
            @RequestBody ThemeUpdateDTO body) {

        Theme t = themeRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Theme " + id + " not found"));

        // 更新 name（如果有傳）
        if (body.getName() != null && !body.getName().trim().isEmpty()) {
            String newName = body.getName().trim();

            // 如果真的改名，檢查重複
            if (!newName.equalsIgnoreCase(t.getName())
                    && themeRepo.existsByName(newName)) {
                throw new IllegalStateException("Theme name already exists");
            }

            t.setName(newName);
        }

        // 更新圖片（允許清空、覆蓋）
        if (body.getImageUrl() != null) {
            t.setImageUrl(body.getImageUrl());
        }

        Theme saved = themeRepo.save(t);

        long usage = itemRepo.countByTheme_Id(saved.getId());

        ThemeDTO dto = new ThemeDTO();
        dto.setId(saved.getId());
        dto.setName(saved.getName());
        dto.setCreatedAt(saved.getCreatedAt());
        dto.setUsageCount(usage);
        dto.setImageUrl(saved.getImageUrl());
        return dto;
    }
}