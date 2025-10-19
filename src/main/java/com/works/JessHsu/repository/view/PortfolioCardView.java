package com.works.JessHsu.repository.view;

import java.sql.Timestamp;

public interface PortfolioCardView {
    Long getId();
    String getTitle();
    String getCategory();
    String getCoverImageUrl();
    Timestamp getCreatedAt(); // 之後在 Service 轉成 LocalDateTime
}