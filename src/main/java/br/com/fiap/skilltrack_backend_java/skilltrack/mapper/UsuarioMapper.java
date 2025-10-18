package br.com.fiap.skilltrack_backend_java.skilltrack.mapper;

import br.com.fiap.skilltrack_backend_java.skilltrack.dto.UsuarioRequestDTO;
import br.com.fiap.skilltrack_backend_java.skilltrack.dto.UsuarioResponseDTO;
import br.com.fiap.skilltrack_backend_java.skilltrack.model.Usuario;

public class UsuarioMapper {

    public static UsuarioResponseDTO toResponseDTO(Usuario usuario) {
        if (usuario == null) return null;

        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getSenha(),
                usuario.getPerfil(),
                usuario.getDataCriacao()
        );
    }

    public static Usuario toEntity(UsuarioRequestDTO dto) {
        if (dto == null) return null;

        Usuario usuario = new Usuario();
        usuario.setNome(dto.getNome());
        usuario.setEmail(dto.getEmail());
        usuario.setSenha(dto.getSenha());
        usuario.setPerfil(dto.getPerfil());
        return usuario;
    }
}

