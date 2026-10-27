package br.com.fiap.skilltrack_backend_java.skilltrack.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GoalRequestDTO {

    @Size(max = 150)
    private String title;

    @Size(max = 255)
    private String description;

    @FutureOrPresent(message = "A data limite deve ser futura ou presente")
    private LocalDate deadline;

    @JsonProperty("skill_id")
    @NotNull(message = "O ID da habilidade é obrigatório")
    private Long skillId;

    @JsonProperty("user_id")
    @NotNull(message = "O ID do usuário é obrigatório")
    private Long userId;

    @JsonIgnore
    @AssertTrue(message = "Informe o título ou a descrição da meta")
    public boolean isTituloOuDescricaoInformado() {
        return (title != null && !title.isBlank()) || (description != null && !description.isBlank());
    }

    // título e descrição usam um ao outro como valor padrão quando não informados
    @JsonIgnore
    public String getTituloEfetivo() {
        if (title != null && !title.isBlank()) return title;
        return description.length() > 150 ? description.substring(0, 147) + "..." : description;
    }

    @JsonIgnore
    public String getDescricaoEfetiva() {
        return description != null && !description.isBlank() ? description : title;
    }
}
