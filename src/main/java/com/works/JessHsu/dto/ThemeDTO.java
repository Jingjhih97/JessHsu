package com.works.JessHsu.dto;

import java.time.LocalDateTime;

public class ThemeDTO {
    private Long id;
    private String name;
    private LocalDateTime createdAt;
    private String imageUrl;
    private Long usageCount; 
    private String description;

    // ✅ 無參數建構子（給 JPA 或序列化用）
    public ThemeDTO() {
    }

    // ✅ 有參數建構子（對應 ThemeController.listAll 用）
    public ThemeDTO(Long id, String name, LocalDateTime createdAt, String imageUrl, Long usageCount, String description) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
        this.usageCount = usageCount;
        this.imageUrl = imageUrl;
        this.description = description;
    }

    // ✅ Getter / Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getUsageCount() {
        return usageCount;
    }

    public void setUsageCount(Long usageCount) {
        this.usageCount = usageCount;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}