package com.works.JessHsu.dto;

import java.time.LocalDateTime;

public class PortfolioImageDTO {

    private Long id;
    private Long itemId;
    private String imageUrl;
    private Boolean isPrimary;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private Integer cropX;
    private Integer cropY;
    private Integer cropSize;

    public PortfolioImageDTO() {}

    public PortfolioImageDTO(Long id, Long itemId, String imageUrl, Boolean isPrimary,
                             Integer sortOrder, LocalDateTime createdAt) {
        this.id = id;
        this.itemId = itemId;
        this.imageUrl = imageUrl;
        this.isPrimary = isPrimary;
        this.sortOrder = sortOrder;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Boolean getIsPrimary() { return isPrimary; }
    public void setIsPrimary(Boolean isPrimary) { this.isPrimary = isPrimary; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Integer getCropX() {
        return cropX;
    }

    public void setCropX(Integer cropX) {
        this.cropX = cropX;
    }

    public Integer getCropY() {
        return cropY;
    }

    public void setCropY(Integer cropY) {
        this.cropY = cropY;
    }

    public Integer getCropSize() {
        return cropSize;
    }

    public void setCropSize(Integer cropSize) {
        this.cropSize = cropSize;
    }
}