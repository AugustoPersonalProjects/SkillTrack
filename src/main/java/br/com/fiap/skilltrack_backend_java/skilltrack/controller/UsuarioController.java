package br.com.fiap.skilltrack_backend_java.skilltrack.controller;

import br.com.fiap.skilltrack_backend_java.skilltrack.dto.UsuarioRequestDTO;
import br.com.fiap.skilltrack_backend_java.skilltrack.dto.UsuarioResponseDTO;
import br.com.fiap.skilltrack_backend_java.skilltrack.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UsuarioResponseDTO>> listarTodos(
            @PageableDefault(size = 10, page = 0, sort = {"id"}) Pageable paginacao) {
        var usuarios = usuarioService.listarTodos(paginacao);
        usuarios.forEach(this::adicionarLinks);
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> buscarPorId(@PathVariable Long id) {
        var usuario = usuarioService.buscarPorId(id);
        adicionarLinks(usuario);
        return ResponseEntity.ok(usuario);
    }

    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> salvar(@RequestBody @Valid UsuarioRequestDTO usuarioDTO, UriComponentsBuilder uriBuilder) {
        var usuario = usuarioService.salvar(usuarioDTO);
        adicionarLinks(usuario);
        var uri = uriBuilder.path("/usuarios/{id}").buildAndExpand(usuario.getId()).toUri();
        return ResponseEntity.created(uri).body(usuario);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> atualizar(@PathVariable Long id, @RequestBody @Valid UsuarioRequestDTO usuarioDTO) {
        var usuarioAtualizado = usuarioService.atualizar(id, usuarioDTO);
        adicionarLinks(usuarioAtualizado);
        return ResponseEntity.ok(usuarioAtualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        usuarioService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    private UsuarioResponseDTO adicionarLinks(UsuarioResponseDTO usuario) {
        usuario.add(linkTo(methodOn(UsuarioController.class).buscarPorId(usuario.getId())).withRel("self"));
        usuario.add(linkTo(methodOn(UsuarioController.class).atualizar(usuario.getId(), null)).withRel("update"));
        usuario.add(linkTo(methodOn(UsuarioController.class).deletar(usuario.getId())).withRel("delete"));
        return usuario;
    }
}
