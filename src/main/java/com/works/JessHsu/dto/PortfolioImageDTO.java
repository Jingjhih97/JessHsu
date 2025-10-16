package com.works.JessHsu.dto;

public record PortfolioImageDTO(
        Long id,
        String imageUrl,
        boolean isPrimary,
        int sortOrder) {
}
