package br.com.fiap.skilltrack_backend_java.skilltrack.dto;

import br.com.fiap.skilltrack_backend_java.skilltrack.model.enums.SkillLevel;
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

    @NotBlank(message = "A categoria é obrigatória")
    @Size(max = 100)
    private String category;

    @NotNull(message = "O nível de habilidade é obrigatório")
    private SkillLevel level;

    @NotNull(message = "O id do usuário é obrigatório")
    private Long userId;
}

