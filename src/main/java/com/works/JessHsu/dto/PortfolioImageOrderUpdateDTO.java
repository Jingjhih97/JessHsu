package com.works.JessHsu.dto;

import jakarta.validation.constraints.NotNull;

public class PortfolioImageOrderUpdateDTO {

    @NotNull
    private Long imageId;     // 要更新的圖片 ID

    @NotNull
    private Integer sortOrder; // 新的排序值

    private Long themeId;

    public PortfolioImageOrderUpdateDTO() {
    }

    public PortfolioImageOrderUpdateDTO(Long imageId, Integer sortOrder) {
        this.imageId = imageId;
        this.sortOrder = sortOrder;
    }

    public Long getImageId() {
        return imageId;
    }

    public void setImageId(Long imageId) {
        this.imageId = imageId;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Long getThemeId() {
        return themeId;
    }

    public void setThemeId(Long themeId) {
        this.themeId = themeId;
    }
}