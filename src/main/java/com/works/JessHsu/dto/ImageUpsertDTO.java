package com.works.JessHsu.dto;

public class ImageUpsertDTO {
    private Long id; // 既有圖片帶 id；新增圖片為 null
    private String imageUrl;
    private Boolean isPrimary;
    private Integer sortOrder;
    private Integer cropX;
    private Integer cropY;
    private Integer cropSize;

    // getters / setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Boolean getIsPrimary() {
        return isPrimary;
    }

    public void setIsPrimary(Boolean isPrimary) {
        this.isPrimary = isPrimary;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

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