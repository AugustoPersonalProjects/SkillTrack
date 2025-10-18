package br.com.fiap.skilltrack_backend_java.skilltrack.dto;

import br.com.fiap.skilltrack_backend_java.skilltrack.model.enums.Perfil;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.*;
import org.springframework.hateoas.RepresentationModel;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonPropertyOrder({"id_usuario", "nome", "email", "perfil", "data_criacao", "_links"})
public class UsuarioResponseDTO extends RepresentationModel<UsuarioResponseDTO> {

    @JsonProperty("id_usuario")
    private Long id;

    private String nome;

    private String email;

    private Perfil perfil;

    @JsonProperty("data_criacao")
    private LocalDateTime dataCriacao;
}
