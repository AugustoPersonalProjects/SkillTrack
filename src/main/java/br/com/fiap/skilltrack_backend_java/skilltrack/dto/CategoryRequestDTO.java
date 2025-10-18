package br.com.fiap.skilltrack_backend_java.skilltrack.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CategoryRequestDTO {

    @NotBlank(message = "O nome da categoria é obrigatório")
    @Size(max = 100)
    private String name;

    @Size(max = 255)
    private String description;
}
