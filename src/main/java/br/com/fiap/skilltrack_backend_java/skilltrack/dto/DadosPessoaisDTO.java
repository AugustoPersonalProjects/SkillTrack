package br.com.fiap.skilltrack_backend_java.skilltrack.dto;

import br.com.fiap.skilltrack_backend_java.skilltrack.model.enums.CorAvatar;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DadosPessoaisDTO(
        @NotBlank(message = "O nome é obrigatório")
        @Size(min = 3, max = 150, message = "O nome deve ter entre 3 e 150 caracteres")
        String nome,

        @Size(max = 280, message = "A bio deve ter no máximo 280 caracteres")
        String bio,

        @JsonProperty("cor_avatar")
        @NotNull(message = "Escolha a cor do avatar")
        CorAvatar corAvatar
) {}
