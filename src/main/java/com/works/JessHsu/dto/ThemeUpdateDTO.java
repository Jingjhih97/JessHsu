package com.works.JessHsu.dto;

public class ThemeUpdateDTO {
    private String name;
    private String imageUrl;

    public ThemeUpdateDTO() {}

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