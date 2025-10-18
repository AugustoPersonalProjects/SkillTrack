package br.com.fiap.skilltrack_backend_java.skilltrack.controller;

import br.com.fiap.skilltrack_backend_java.skilltrack.dto.CategoryRequestDTO;
import br.com.fiap.skilltrack_backend_java.skilltrack.dto.CategoryResponseDTO;
import br.com.fiap.skilltrack_backend_java.skilltrack.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public ResponseEntity<Page<CategoryResponseDTO>> listarTodos(
            @PageableDefault(size = 10, page = 0, sort = {"id"}) Pageable paginacao) {
        var items = categoryService.listarTodos(paginacao);
        items.forEach(this::adicionarLinks);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> buscarPorId(@PathVariable Long id) {
        var item = categoryService.buscarPorId(id);
        adicionarLinks(item);
        return ResponseEntity.ok(item);
    }

    @PostMapping
    public ResponseEntity<CategoryResponseDTO> salvar(@RequestBody @Valid CategoryRequestDTO dto, UriComponentsBuilder uriBuilder) {
        var salvo = categoryService.salvar(dto);
        adicionarLinks(salvo);
        var uri = uriBuilder.path("/categories/{id}").buildAndExpand(salvo.getId()).toUri();
        return ResponseEntity.created(uri).body(salvo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> atualizar(@PathVariable Long id, @RequestBody @Valid CategoryRequestDTO dto) {
        var atualizado = categoryService.atualizar(id, dto);
        adicionarLinks(atualizado);
        return ResponseEntity.ok(atualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        categoryService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    private CategoryResponseDTO adicionarLinks(CategoryResponseDTO dto) {
        dto.add(linkTo(methodOn(CategoryController.class).buscarPorId(dto.getId())).withRel("self"));
        dto.add(linkTo(methodOn(CategoryController.class).atualizar(dto.getId(), null)).withRel("update"));
        dto.add(linkTo(methodOn(CategoryController.class).deletar(dto.getId())).withRel("delete"));
        return dto;
    }
}
