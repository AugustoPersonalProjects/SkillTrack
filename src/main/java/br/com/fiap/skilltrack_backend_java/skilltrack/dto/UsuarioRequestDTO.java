package br.com.fiap.skilltrack_backend_java.skilltrack.dto;

import br.com.fiap.skilltrack_backend_java.skilltrack.model.enums.Perfil;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioRequestDTO {

    @NotBlank(message = "O nome é obrigatório")
    @Size(min = 3, max = 150)
    private String nome;

    @NotBlank(message = "O e-mail é obrigatório")
    @Email
    @Size(max = 150)
    private String email;

    @NotBlank(message = "A senha é obrigatória")
    @Size(min = 6, max = 150)
    private String senha;

    @NotNull(message = "O perfil do usuário é obrigatório")
    private Perfil perfil;
}
