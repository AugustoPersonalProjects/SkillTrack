package br.com.fiap.skilltrack_backend_java.skilltrack.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProgressRequestDTO {

    @NotNull(message = "O id da habilidade é obrigatório")
    private Long skillId;

    @NotNull(message = "A porcentagem de progresso é obrigatória")
    private Double percentage;
}
