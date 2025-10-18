package com.skilltrack.dto;

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

    @NotNull(message = "A data limite é obrigatória")
    private LocalDate deadline;

    @NotNull(message = "O id da habilidade é obrigatório")
    private Long skillId;
}
