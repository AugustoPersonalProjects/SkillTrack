package br.com.fiap.skilltrack_backend_java.skilltrack.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AlteracaoSenhaDTO(
        @JsonProperty("senha_atual")
        @NotBlank(message = "Informe a senha atual")
        String senhaAtual,

        @JsonProperty("nova_senha")
        @NotBlank(message = "Informe a nova senha")
        @Size(min = 6, max = 72, message = "A senha deve ter entre 6 e 72 caracteres")
        String novaSenha
) {}
