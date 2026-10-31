package br.com.fiap.skilltrack_backend_java.skilltrack.dto;

import br.com.fiap.skilltrack_backend_java.skilltrack.model.enums.Tema;
import jakarta.validation.constraints.NotNull;

public record AlteracaoTemaDTO(@NotNull(message = "Escolha o tema") Tema tema) {}
