package br.com.fiap.skilltrack_backend_java.skilltrack.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.hateoas.RepresentationModel;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CategoryResponseDTO extends RepresentationModel<CategoryResponseDTO> {

    @JsonProperty("id_category")
    private Long id;

    private String name;

    private String description;
}
