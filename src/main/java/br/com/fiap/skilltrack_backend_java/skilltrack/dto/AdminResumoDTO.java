package br.com.fiap.skilltrack_backend_java.skilltrack.dto;

import br.com.fiap.skilltrack_backend_java.skilltrack.model.enums.CorAvatar;
import br.com.fiap.skilltrack_backend_java.skilltrack.model.enums.Perfil;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;

public record AdminResumoDTO(
        long totalUsuarios,
        long totalAdmins,
        long totalSkills,
        long totalGoals,
        long completedGoals,
        long totalProgress,
        long totalCategories,
        List<Usuario> usuarios
) {
    public record Usuario(
            @JsonProperty("id_usuario") Long id,
            String nome,
            String email,
            Perfil perfil,
            @JsonProperty("data_criacao") Instant dataCriacao,
            @JsonProperty("cor_avatar") CorAvatar corAvatar,
            long totalSkills,
            long totalGoals,
            long completedGoals
    ) {}
}
