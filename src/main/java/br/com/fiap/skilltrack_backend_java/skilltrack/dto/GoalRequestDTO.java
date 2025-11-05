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

    @NotBlank(message = "A descrição da meta é obrigatória")
    private String description;

    @FutureOrPresent(message = "A data limite deve ser futura ou presente")
    private LocalDate deadline;

    @JsonProperty("skill_id")
    @NotNull(message = "O ID da habilidade é obrigatório")
    private Long skillId;

    @JsonProperty("user_id")
    @NotNull(message = "O ID do usuário é obrigatório")
    private Long userId;
}
