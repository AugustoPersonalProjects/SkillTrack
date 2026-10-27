package br.com.fiap.skilltrack_backend_java.skilltrack.dto;

import br.com.fiap.skilltrack_backend_java.skilltrack.model.enums.SkillLevel;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.*;
import org.springframework.hateoas.RepresentationModel;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonPropertyOrder({"id_skill", "name", "description", "category_id", "level", "progress_history", "goals", "_links"})
public class SkillResponseDTO extends RepresentationModel<SkillResponseDTO> {

    @JsonProperty("id_skill")
    private Long id;

    private String name;

    private String description;

    @JsonProperty("category_id")
    private Long categoryId;

    private SkillLevel level;

    @JsonProperty("progress_history")
    private List<ProgressResponseDTO> progressHistory;

    private List<GoalResponseDTO> goals;
}
