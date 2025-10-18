package br.com.fiap.skilltrack_backend_java.skilltrack.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.*;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonPropertyOrder({"id_goal", "description", "deadline", "completed", "skill_id", "_links"})
public class GoalResponseDTO extends RepresentationModel<GoalResponseDTO> {

    @JsonProperty("id_goal")
    private Long id;

    private String description;

    private LocalDate deadline;

    private boolean completed;

    @JsonProperty("skill_id")
    private Long skillId;
}
