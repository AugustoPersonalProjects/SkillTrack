package br.com.fiap.skilltrack_backend_java.skilltrack.service;

import br.com.fiap.skilltrack_backend_java.skilltrack.dto.CategoryRequestDTO;
import br.com.fiap.skilltrack_backend_java.skilltrack.dto.CategoryResponseDTO;
import br.com.fiap.skilltrack_backend_java.skilltrack.mapper.CategoryMapper;
import br.com.fiap.skilltrack_backend_java.skilltrack.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public Page<CategoryResponseDTO> listarTodos(Pageable pageable) {
        return categoryRepository.findAllByOrderByIdAsc(pageable)
                .map(CategoryMapper::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public CategoryResponseDTO buscarPorId(Long id) {
        var category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category com id: " + id + " não encontrada"));
        return CategoryMapper.toResponseDTO(category);
    }

    @Transactional
    public CategoryResponseDTO salvar(CategoryRequestDTO dto) {
        var category = CategoryMapper.toEntity(dto);
        var salvo = categoryRepository.save(category);
        return CategoryMapper.toResponseDTO(salvo);
    }

    @Transactional
    public CategoryResponseDTO atualizar(Long id, CategoryRequestDTO dto) {
        var categoryAtual = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category com id: " + id + " não encontrada"));

        categoryAtual.setName(dto.getName());
        categoryAtual.setDescription(dto.getDescription());
        var atualizado = categoryRepository.save(categoryAtual);
        return CategoryMapper.toResponseDTO(atualizado);
    }

    @Transactional
    public void deletar(Long id) {
        var categoria = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category com id: " + id + " não encontrada"));
        categoryRepository.delete(categoria);
    }
}
