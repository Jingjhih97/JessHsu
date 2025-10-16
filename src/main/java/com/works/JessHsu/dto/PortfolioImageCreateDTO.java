package com.works.JessHsu.dto;


public record PortfolioImageCreateDTO(
        String imageUrl,
        Boolean isPrimary,
        Integer sortOrder) {

    public PortfolioImageCreateDTO(String imageUrl, Boolean isPrimary, Integer sortOrder) {
        this.imageUrl = imageUrl;
        this.isPrimary = isPrimary;
        this.sortOrder = sortOrder;
    }

}