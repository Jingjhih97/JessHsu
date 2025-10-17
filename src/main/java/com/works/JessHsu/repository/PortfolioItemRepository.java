package com.works.JessHsu.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.works.JessHsu.dto.PortfolioCardDTO;
import com.works.JessHsu.entity.PortfolioItem;

public interface PortfolioItemRepository extends JpaRepository<PortfolioItem, Long> {

  @Query(value = """
      SELECT new com.works.JessHsu.dto.PortfolioCardDTO(
          p.id,
          p.title,
          COALESCE(
              MAX(CASE WHEN img.isPrimary = TRUE THEN img.imageUrl END),
              MAX(img.imageUrl),
              ''
          )
      )
      FROM PortfolioItem p
      LEFT JOIN p.images img
      WHERE (:onlyPublished IS NULL OR p.published = :onlyPublished)
        AND (:category IS NULL OR p.category = :category)
      GROUP BY p.id, p.title
      """, countQuery = """
      SELECT COUNT(p)
      FROM PortfolioItem p
      WHERE (:onlyPublished IS NULL OR p.published = :onlyPublished)
        AND (:category IS NULL OR p.category = :category)
      """)
  Page<PortfolioCardDTO> searchCards(@Param("onlyPublished") Boolean onlyPublished,
      @Param("category") String category,
      Pageable pageable);

  @Query("SELECT p FROM PortfolioItem p LEFT JOIN FETCH p.images WHERE p.id = :id")
  Optional<PortfolioItem> findByIdWithImages(@Param("id") Long id);

  @Query("""
      SELECT p FROM PortfolioItem p
      WHERE (:onlyPublished IS NULL OR p.published = :onlyPublished)
        AND (:category IS NULL OR p.category = :category)
      ORDER BY p.createdAt DESC
      """)
  Page<PortfolioItem> search(@Param("onlyPublished") Boolean onlyPublished,
      @Param("category") String category,
      Pageable pageable);

  Page<PortfolioItem> findByPublishedTrue(Pageable pageable);

  Page<PortfolioItem> findByCategoryAndPublishedTrue(String category, Pageable pageable);
}