package br.com.fiap.skilltrack_backend_java.skilltrack.mapper;

import br.com.fiap.skilltrack_backend_java.skilltrack.dto.CategoryRequestDTO;
import br.com.fiap.skilltrack_backend_java.skilltrack.dto.CategoryResponseDTO;
import br.com.fiap.skilltrack_backend_java.skilltrack.model.Category;

public class CategoryMapper {

    public static CategoryResponseDTO toResponseDTO(Category category) {
        if (category == null) return null;

        return new CategoryResponseDTO(
                category.getId(),
                category.getName(),
                category.getDescription()
        );
    }

    public static Category toEntity(CategoryRequestDTO dto) {
        if (dto == null) return null;

        Category category = new Category();
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        return category;
    }
}

