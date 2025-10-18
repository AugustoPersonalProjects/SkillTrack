package br.com.fiap.skilltrack_backend_java.skilltrack.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProgressRequestDTO {

    @JsonProperty("skill_id")
    @NotNull(message = "O ID da habilidade é obrigatório")
    private Long skillId;

    @Min(0)
    @Max(100)
    private int percentage;

    private LocalDate date;
}
