package com.works.JessHsu.dto;

import java.time.LocalDateTime;


public class PortfolioItemDTO {
    private Long id;
    private String title;
    private String description;
    private String coverImageUrl;
    private String category;
    private Boolean published;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PortfolioItemDTO() {
    }

    public PortfolioItemDTO(String category, String coverImageUrl, LocalDateTime createdAt, String description, Long id, Boolean published, String title, LocalDateTime updatedAt) {
        this.category = category;
        this.coverImageUrl = coverImageUrl;
        this.createdAt = createdAt;
        this.description = description;
        this.id = id;
        this.published = published;
        this.title = title;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Boolean getPublished() {
        return published;
    }

    public void setPublished(Boolean published) {
        this.published = published;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    

}
