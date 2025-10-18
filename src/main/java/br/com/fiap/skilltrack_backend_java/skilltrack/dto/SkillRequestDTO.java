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

    @NotNull(message = "O ID da categoria é obrigatório")
    private Long categoryId;

    @NotNull(message = "O nível da habilidade é obrigatório")
    private SkillLevel level;

    @NotNull(message = "O ID do usuário é obrigatório")
    private Long userId;
}
