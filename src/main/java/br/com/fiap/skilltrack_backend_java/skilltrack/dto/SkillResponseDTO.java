package br.com.fiap.skilltrack_backend_java.skilltrack.dto;

import br.com.fiap.skilltrack_backend_java.skilltrack.model.enums.SkillLevel;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.hateoas.RepresentationModel;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SkillResponseDTO extends RepresentationModel<SkillResponseDTO> {

    @JsonProperty("id_skill")
    private Long id;

    private String name;

    private String category;

    private SkillLevel level;

    private List<ProgressResponseDTO> progressHistory;

    private List<GoalResponseDTO> goals;
}
