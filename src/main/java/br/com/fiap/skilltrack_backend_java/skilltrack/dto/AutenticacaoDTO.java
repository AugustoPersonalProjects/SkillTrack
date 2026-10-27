package br.com.fiap.skilltrack_backend_java.skilltrack.dto;

import jakarta.validation.constraints.NotBlank;

public record AutenticacaoDTO(@NotBlank String email, @NotBlank String senha) {}
