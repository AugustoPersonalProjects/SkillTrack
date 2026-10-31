package br.com.fiap.skilltrack_backend_java.skilltrack.dto;

import br.com.fiap.skilltrack_backend_java.skilltrack.model.enums.Perfil;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Na atualização a senha é opcional: vazia ou ausente mantém a senha atual
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioUpdateDTO {

    @NotBlank(message = "O nome é obrigatório")
    @Size(min = 3, max = 150)
    private String nome;

    @NotBlank(message = "O e-mail é obrigatório")
    @Email
    @Size(max = 150)
    private String email;

    @Pattern(regexp = "^$|^.{6,72}$", message = "A senha deve ter entre 6 e 72 caracteres")
    private String senha;

    // opcional: só é respeitado quando quem faz a requisição é ADMIN
    private Perfil perfil;
}
