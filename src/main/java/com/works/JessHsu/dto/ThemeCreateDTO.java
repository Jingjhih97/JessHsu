package com.works.JessHsu.dto;

public class ThemeCreateDTO {

    private String name;
    private String imageUrl;

    public ThemeCreateDTO() {
    }

    public ThemeCreateDTO(String name, String imageUrl) {
        this.name = name;
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}