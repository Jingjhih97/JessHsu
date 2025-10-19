package com.works.JessHsu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PortfolioItemCreateDTO {

    @NotBlank
    @Size(max = 100)
    private String title;

    @Size(max = 500)
    private String description;

    @Size(max = 255)
    private String coverImageUrl;

    @Size(max = 100)
    private String category;

    private Boolean published;

    public PortfolioItemCreateDTO() {}

    public PortfolioItemCreateDTO(String title, String description, String coverImageUrl, String category, Boolean published) {
        this.title = title;
        this.description = description;
        this.coverImageUrl = coverImageUrl;
        this.category = category;
        this.published = published;
    }

    // Getters & Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCoverImageUrl() { return coverImageUrl; }
    public void setCoverImageUrl(String coverImageUrl) { this.coverImageUrl = coverImageUrl; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Boolean getPublished() { return published; }
    public void setPublished(Boolean published) { this.published = published; }
}