package com.works.JessHsu.dto;

import java.time.LocalDateTime;

public class PortfolioCardDto {
    private Long id;
    private String title;
    private String category;
    private String coverImageUrl; // 來自 images：主圖或第一張
    private LocalDateTime createdAt;

    public PortfolioCardDto() {}

    public PortfolioCardDto(Long id, String title, String category, String coverImageUrl, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.coverImageUrl = coverImageUrl;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getCategory() { return category; }
    public String getCoverImageUrl() { return coverImageUrl; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setId(Long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setCategory(String category) { this.category = category; }
    public void setCoverImageUrl(String coverImageUrl) { this.coverImageUrl = coverImageUrl; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}