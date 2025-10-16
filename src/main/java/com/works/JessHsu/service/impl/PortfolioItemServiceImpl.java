package com.works.JessHsu.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.works.JessHsu.dto.PortfolioItemCreateDTO;
import com.works.JessHsu.dto.PortfolioItemDTO;
import com.works.JessHsu.entity.PortfolioItem;
import com.works.JessHsu.mapper.PortfolioItemMapper;
import com.works.JessHsu.repository.PortfolioItemRepository;
import com.works.JessHsu.service.PortfolioItemService;


@Service
@Transactional
public class PortfolioItemServiceImpl implements PortfolioItemService {
    private final PortfolioItemRepository repo;

    public PortfolioItemServiceImpl(PortfolioItemRepository repo) {
        this.repo = repo;
    }

    @Override
    public PortfolioItemDTO create(PortfolioItemCreateDTO dto) {
        PortfolioItem e = PortfolioItemMapper.toEntity(dto);
        return PortfolioItemMapper.toDTO(repo.save(e));
    }

    @Override
    public PortfolioItemDTO update(Long id, PortfolioItemCreateDTO dto) {
        PortfolioItem e = repo.findById(id)
        .orElseThrow(() -> new RuntimeException("Item not found"));
        PortfolioItemMapper.updateEntity(e, dto);
        return PortfolioItemMapper.toDTO(repo.save(e));
    }

    @Override
    public void delete(Long id) {
        if (!repo.existsById(id))
            throw new RuntimeException("Item not found");
        repo.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public PortfolioItemDTO get(Long id) {
        return repo.findById(id)
                .map(PortfolioItemMapper::toDTO)
                .orElseThrow(() -> new RuntimeException("Item not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PortfolioItemDTO> list(Pageable pageable, Boolean onlyPublished, String category) {
        if (Boolean.TRUE.equals(onlyPublished) && category != null && !category.isBlank()) {
            return repo.findByCategoryAndPublishedTrue(category, pageable).map(PortfolioItemMapper::toDTO);
        }
        if (Boolean.TRUE.equals(onlyPublished)) {
            return repo.findByPublishedTrue(pageable).map(PortfolioItemMapper::toDTO);
        }
        return repo.findAll(pageable).map(PortfolioItemMapper::toDTO);
    }
}
