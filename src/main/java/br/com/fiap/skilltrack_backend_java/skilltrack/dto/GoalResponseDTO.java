package br.com.fiap.skilltrack_backend_java.skilltrack.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.hateoas.RepresentationModel;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GoalResponseDTO extends RepresentationModel<GoalResponseDTO> {

    @JsonProperty("id_goal")
    private Long id;

    private String description;

    private LocalDate deadline;

    @JsonProperty("skill_id")
    private Long skillId;
}
