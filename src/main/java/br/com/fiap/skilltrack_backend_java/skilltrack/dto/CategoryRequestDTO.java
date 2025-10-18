package br.com.fiap.skilltrack_backend_java.skilltrack.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CategoryRequestDTO {

    @NotBlank(message = "O nome da categoria é obrigatório")
    private String name;

    private String description;
}
