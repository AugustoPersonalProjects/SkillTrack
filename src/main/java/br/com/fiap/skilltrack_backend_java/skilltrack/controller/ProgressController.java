package br.com.fiap.skilltrack_backend_java.skilltrack.controller;

import br.com.fiap.skilltrack_backend_java.skilltrack.dto.ProgressRequestDTO;
import br.com.fiap.skilltrack_backend_java.skilltrack.dto.ProgressResponseDTO;
import br.com.fiap.skilltrack_backend_java.skilltrack.service.ProgressService;
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
@RequestMapping("/progress")
public class ProgressController {

    @Autowired
    private ProgressService progressService;

    @GetMapping
    public ResponseEntity<Page<ProgressResponseDTO>> listarTodos(
            @PageableDefault(size = 10, page = 0, sort = {"id"}) Pageable paginacao) {
        var items = progressService.listarTodos(paginacao);
        items.forEach(this::adicionarLinks);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProgressResponseDTO> buscarPorId(@PathVariable Long id) {
        var item = progressService.buscarPorId(id);
        adicionarLinks(item);
        return ResponseEntity.ok(item);
    }

    @PostMapping
    public ResponseEntity<ProgressResponseDTO> salvar(@RequestBody @Valid ProgressRequestDTO dto, UriComponentsBuilder uriBuilder) {
        var salvo = progressService.salvar(dto);
        adicionarLinks(salvo);
        var uri = uriBuilder.path("/progress/{id}").buildAndExpand(salvo.getId()).toUri();
        return ResponseEntity.created(uri).body(salvo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProgressResponseDTO> atualizar(@PathVariable Long id, @RequestBody @Valid ProgressRequestDTO dto) {
        var atualizado = progressService.atualizar(id, dto);
        adicionarLinks(atualizado);
        return ResponseEntity.ok(atualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        progressService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    private ProgressResponseDTO adicionarLinks(ProgressResponseDTO dto) {
        dto.add(linkTo(methodOn(ProgressController.class).buscarPorId(dto.getId())).withRel("self"));
        dto.add(linkTo(methodOn(ProgressController.class).atualizar(dto.getId(), null)).withRel("update"));
        dto.add(linkTo(methodOn(ProgressController.class).deletar(dto.getId())).withRel("delete"));
        return dto;
    }
}
