package br.com.fiap.skilltrack_backend_java.skilltrack.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SkillRequestDTO {

    @NotBlank(message = "O nome da habilidade é obrigatório")
    @Size(max = 100)
    private String name;

    @NotNull(message = "A categoria é obrigatória")
    private Long category; // ID da categoria associada

    @NotNull(message = "O nível inicial é obrigatório")
    private Integer level;
}
