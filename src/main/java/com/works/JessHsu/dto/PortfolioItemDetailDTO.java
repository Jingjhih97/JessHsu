package com.works.JessHsu.dto;

import java.time.LocalDateTime;
import java.util.List;

public record PortfolioItemDetailDTO(
  Long id,
  String title,
  String description,
  String category,
  Boolean published,
  LocalDateTime createdAt,
  LocalDateTime updatedAt,
  List<PortfolioImageDTO> images
) {}
