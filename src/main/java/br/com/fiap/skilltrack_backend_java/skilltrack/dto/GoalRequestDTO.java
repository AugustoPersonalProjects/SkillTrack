package br.com.fiap.skilltrack_backend_java.skilltrack.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GoalRequestDTO {

    @NotBlank(message = "A descrição é obrigatória")
    private String description;

    @FutureOrPresent(message = "A data limite deve ser futura")
    private LocalDate deadline;

    @JsonProperty("skill_id")
    @NotNull(message = "O ID da habilidade é obrigatório")
    private Long skillId;

    private boolean completed;
}
