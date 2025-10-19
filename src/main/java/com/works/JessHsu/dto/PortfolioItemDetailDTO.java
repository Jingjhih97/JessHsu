package com.works.JessHsu.dto;

import java.time.LocalDateTime;
import java.util.List;

public class PortfolioItemDetailDTO {
    private Long id;
    private String title;
    private String description;
    private String category;
    private Boolean published;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<PortfolioImageDTO> images;

    public PortfolioItemDetailDTO() {}

    public PortfolioItemDetailDTO(Long id, String title, String description, String category,
                                  Boolean published, LocalDateTime createdAt, LocalDateTime updatedAt,
                                  List<PortfolioImageDTO> images) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.published = published;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.images = images;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public Boolean getPublished() { return published; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public List<PortfolioImageDTO> getImages() { return images; }

    public void setId(Long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setCategory(String category) { this.category = category; }
    public void setPublished(Boolean published) { this.published = published; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public void setImages(List<PortfolioImageDTO> images) { this.images = images; }
}
