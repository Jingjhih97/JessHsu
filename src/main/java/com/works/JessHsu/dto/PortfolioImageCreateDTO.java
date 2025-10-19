package com.works.JessHsu.dto;

import jakarta.validation.constraints.*;

public class PortfolioImageCreateDTO {
    @NotBlank
    @Size(max = 500)
    private String imageUrl;

    private Boolean isPrimary = false;

    @NotNull
    private Integer sortOrder = 0;

    public PortfolioImageCreateDTO() {}

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
}