package br.com.fiap.skilltrack_backend_java.skilltrack.controller;

import br.com.fiap.skilltrack_backend_java.skilltrack.dto.GoalRequestDTO;
import br.com.fiap.skilltrack_backend_java.skilltrack.dto.GoalResponseDTO;
import br.com.fiap.skilltrack_backend_java.skilltrack.service.GoalService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/goals")
public class GoalController {

    @Autowired
    private GoalService goalService;

    @GetMapping
    public ResponseEntity<Page<GoalResponseDTO>> listarTodos(
            @PageableDefault(size = 10, page = 0, sort = {"id"}) Pageable paginacao) {
        var goals = goalService.listarTodos(paginacao);
        goals.forEach(this::adicionarLinks);
        return ResponseEntity.ok(goals);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GoalResponseDTO> buscarPorId(@PathVariable Long id) {
        var goal = goalService.buscarPorId(id);
        adicionarLinks(goal);
        return ResponseEntity.ok(goal);
    }

    @PostMapping
    public ResponseEntity<GoalResponseDTO> salvar(@RequestBody @Valid GoalRequestDTO dto, UriComponentsBuilder uriBuilder) {
        var goal = goalService.salvar(dto);
        adicionarLinks(goal);
        var uri = uriBuilder.path("/goals/{id}").buildAndExpand(goal.getId()).toUri();
        return ResponseEntity.created(uri).body(goal);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GoalResponseDTO> atualizar(@PathVariable Long id, @RequestBody @Valid GoalRequestDTO dto) {
        var atualizado = goalService.atualizar(id, dto);
        adicionarLinks(atualizado);
        return ResponseEntity.ok(atualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        goalService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<Void> marcarComoCompleta(@PathVariable Long id) {
        goalService.marcarComoCompleta(id);
        return ResponseEntity.noContent().build();
    }

    private GoalResponseDTO adicionarLinks(GoalResponseDTO dto) {
        dto.add(linkTo(methodOn(GoalController.class).buscarPorId(dto.getId())).withRel("self"));
        dto.add(linkTo(methodOn(GoalController.class).atualizar(dto.getId(), null)).withRel("update"));
        dto.add(linkTo(methodOn(GoalController.class).deletar(dto.getId())).withRel("delete"));
        dto.add(linkTo(methodOn(GoalController.class).marcarComoCompleta(dto.getId())).withRel("complete"));
        return dto;
    }
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<GoalResponseDTO>> buscarPorUsuario(@PathVariable Long userId) {
        var goals = goalService.buscarPorUsuario(userId);
        goals.forEach(this::adicionarLinks);
        return ResponseEntity.ok(goals);
    }
}