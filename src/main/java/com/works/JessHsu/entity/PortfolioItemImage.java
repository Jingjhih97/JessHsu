package com.works.JessHsu.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "portfolio_item_images")
public class PortfolioItemImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 關聯 portfolio_items 表
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private PortfolioItem item;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "is_primary", nullable = false)
    private Boolean isPrimary = false;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ---- Constructors ----
    public PortfolioItemImage() {}

    public PortfolioItemImage(PortfolioItem item, String imageUrl, Boolean isPrimary, Integer sortOrder) {
        this.item = item;
        this.imageUrl = imageUrl;
        this.isPrimary = isPrimary;
        this.sortOrder = sortOrder;
    }

    // ---- Getters / Setters ----
    public Long getId() {
        return id;
    }

    public PortfolioItem getItem() {
        return item;
    }

    public void setItem(PortfolioItem item) {
        this.item = item;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}