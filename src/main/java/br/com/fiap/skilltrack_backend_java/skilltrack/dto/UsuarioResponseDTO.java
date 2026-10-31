package br.com.fiap.skilltrack_backend_java.skilltrack.dto;

import br.com.fiap.skilltrack_backend_java.skilltrack.model.enums.CorAvatar;
import br.com.fiap.skilltrack_backend_java.skilltrack.model.enums.Perfil;
import br.com.fiap.skilltrack_backend_java.skilltrack.model.enums.Tema;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.*;
import org.springframework.hateoas.RepresentationModel;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonPropertyOrder({"id_usuario", "nome", "email", "perfil", "data_criacao", "bio", "cor_avatar", "tema", "_links"})
public class UsuarioResponseDTO extends RepresentationModel<UsuarioResponseDTO> {

    @JsonProperty("id_usuario")
    private Long id;

    private String nome;

    private String email;

    private Perfil perfil;

    @JsonProperty("data_criacao")
    private LocalDateTime dataCriacao;

    private String bio;

    @JsonProperty("cor_avatar")
    private CorAvatar corAvatar;

    private Tema tema;
}
