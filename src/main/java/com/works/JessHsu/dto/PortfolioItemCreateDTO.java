// src/main/java/com/works/JessHsu/dto/PortfolioItemCreateDTO.java
package com.works.JessHsu.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PortfolioItemCreateDTO {

    @NotBlank
    @Size(max = 100)
    private String title;

    // @Size(max = 500)
    private String description;

    @Size(max = 255)
    private String coverImageUrl;

    @Size(max = 100)
    private String category;

    private Boolean published;

    // ⭐ 新增：一口氣帶入最多 5 張圖片
    @Size(max = 5, message = "最多只能新增 5 張圖片")
    private List<PortfolioImageCreateDTO> images;

    public PortfolioItemCreateDTO() {}

    public PortfolioItemCreateDTO(String title, String description, String coverImageUrl,
                                  String category, Boolean published,
                                  List<PortfolioImageCreateDTO> images) {
        this.title = title;
        this.description = description;
        this.coverImageUrl = coverImageUrl;
        this.category = category;
        this.published = published;
        this.images = images;
    }

    // getters/setters
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

    public List<PortfolioImageCreateDTO> getImages() { return images; }
    public void setImages(List<PortfolioImageCreateDTO> images) { this.images = images; }
}