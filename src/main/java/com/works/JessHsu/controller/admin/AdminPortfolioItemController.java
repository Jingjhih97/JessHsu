package com.works.JessHsu.controller.admin;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.works.JessHsu.dto.PortfolioImageCreateDTO;
import com.works.JessHsu.dto.PortfolioImageDTO;
import com.works.JessHsu.dto.PortfolioImageOrderUpdateDTO;
import com.works.JessHsu.dto.PortfolioItemCreateDTO;
import com.works.JessHsu.dto.PortfolioItemDTO;
import com.works.JessHsu.dto.PortfolioItemDetailDTO;
import com.works.JessHsu.service.PortfolioItemService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/portfolioItems")
public class AdminPortfolioItemController {

    private final PortfolioItemService service;

    public AdminPortfolioItemController(PortfolioItemService service) {
        this.service = service;
    }

    /* === CRUD === */
    @GetMapping
    public Page<PortfolioItemDTO> list(@RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "10") int size,
                                       @RequestParam(required = false) Boolean onlyPublished,
                                       @RequestParam(required = false) String category) {
        return service.list(PageRequest.of(page, size), onlyPublished, category);
    }

    @GetMapping("/{id}")
    public PortfolioItemDetailDTO get(@PathVariable Long id) {
        return service.getDetail(id);
    }

    @PostMapping
    public PortfolioItemDTO create(@Valid @RequestBody PortfolioItemCreateDTO dto) {
        return service.create(dto);
    }

    @PutMapping("/{id}")
    public PortfolioItemDTO update(@PathVariable Long id, @Valid @RequestBody PortfolioItemCreateDTO dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    /* === 發布控制 === */
    @PutMapping("/{id}/published")
    public void setPublished(@PathVariable Long id, @RequestParam boolean published) {
        service.setPublished(id, published);
    }

    /* === 圖片管理 === */
    @GetMapping("/{id}/images")
    public List<PortfolioImageDTO> listImages(@PathVariable Long id) {
        return service.listImages(id);
    }

    @PostMapping("/{id}/images")
    public PortfolioImageDTO addImage(@PathVariable Long id, @RequestBody PortfolioImageCreateDTO dto) {
        return service.addImage(id, dto);
    }

    @DeleteMapping("/{id}/images/{imageId}")
    public void removeImage(@PathVariable Long id, @PathVariable Long imageId) {
        service.removeImage(id, imageId);
    }

    @PutMapping("/{id}/images/{imageId}/primary")
    public void setPrimaryImage(@PathVariable Long id, @PathVariable Long imageId) {
        service.setPrimaryImage(id, imageId);
    }

    @PutMapping("/{id}/images/reorder")
    public void reorderImages(@PathVariable Long id, @RequestBody List<PortfolioImageOrderUpdateDTO> orders) {
        service.reorderImages(id, orders);
    }
}