package br.com.fiap.skilltrack_backend_java.skilltrack.controller;

import br.com.fiap.skilltrack_backend_java.skilltrack.dto.SkillRequestDTO;
import br.com.fiap.skilltrack_backend_java.skilltrack.dto.SkillResponseDTO;
import br.com.fiap.skilltrack_backend_java.skilltrack.service.SkillService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/skills")
public class SkillController {

    @Autowired
    private SkillService skillService;

    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<SkillResponseDTO>>> listarTodos(Pageable pageable) {
        Page<SkillResponseDTO> page = skillService.listarTodos(pageable);

        var skillModels = page.map(skill -> {
            var model = EntityModel.of(skill);
            model.add(linkTo(methodOn(SkillController.class).buscarPorId(skill.getId())).withSelfRel());
            return model;
        });

        var pagedModel = PagedModel.of(
                skillModels.getContent(),
                new PagedModel.PageMetadata(page.getSize(), page.getNumber(), page.getTotalElements())
        );
        pagedModel.add(linkTo(methodOn(SkillController.class).listarTodos(pageable)).withSelfRel());

        return ResponseEntity.ok(pagedModel);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<SkillResponseDTO>> buscarPorId(@PathVariable Long id) {
        SkillResponseDTO skill = skillService.buscarPorId(id);

        var model = EntityModel.of(skill);
        model.add(linkTo(methodOn(SkillController.class).buscarPorId(id)).withSelfRel());
        model.add(linkTo(methodOn(SkillController.class).listarTodos(Pageable.unpaged())).withRel("all-skills"));

        return ResponseEntity.ok(model);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<SkillResponseDTO>> listarPorUsuario(@PathVariable Long userId) {
        return ResponseEntity.ok(skillService.listarPorUsuario(userId));
    }

    @PostMapping
    public ResponseEntity<EntityModel<SkillResponseDTO>> criar(@Valid @RequestBody SkillRequestDTO dto) {
        SkillResponseDTO criada = skillService.salvar(dto);

        var model = EntityModel.of(criada);
        model.add(linkTo(methodOn(SkillController.class).buscarPorId(criada.getId())).withSelfRel());
        model.add(linkTo(methodOn(SkillController.class).listarTodos(Pageable.unpaged())).withRel("all-skills"));

        return ResponseEntity
                .created(model.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(model);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<SkillResponseDTO>> atualizar(@PathVariable Long id,
                                                                   @Valid @RequestBody SkillRequestDTO dto) {
        SkillResponseDTO atualizada = skillService.atualizar(id, dto);

        var model = EntityModel.of(atualizada);
        model.add(linkTo(methodOn(SkillController.class).buscarPorId(atualizada.getId())).withSelfRel());
        model.add(linkTo(methodOn(SkillController.class).listarTodos(Pageable.unpaged())).withRel("all-skills"));

        return ResponseEntity.ok(model);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        skillService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
