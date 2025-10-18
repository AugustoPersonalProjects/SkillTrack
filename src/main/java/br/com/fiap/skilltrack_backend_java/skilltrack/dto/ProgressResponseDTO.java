package br.com.fiap.skilltrack_backend_java.skilltrack.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.hateoas.RepresentationModel;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProgressResponseDTO extends RepresentationModel<ProgressResponseDTO> {

    @JsonProperty("id_progress")
    private Long id;

    private Double percentage;

    private LocalDateTime date;

    @JsonProperty("skill_id")
    private Long skillId;
}
