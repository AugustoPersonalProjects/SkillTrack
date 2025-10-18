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
@JsonPropertyOrder({"id_progress", "percentage", "date", "skill_id", "_links"})
public class ProgressResponseDTO extends RepresentationModel<ProgressResponseDTO> {

    @JsonProperty("id_progress")
    private Long id;

    private int percentage;

    private LocalDate date;

    @JsonProperty("skill_id")
    private Long skillId;
}
