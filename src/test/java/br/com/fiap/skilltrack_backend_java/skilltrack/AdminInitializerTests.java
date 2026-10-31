package br.com.fiap.skilltrack_backend_java.skilltrack;

import br.com.fiap.skilltrack_backend_java.skilltrack.config.AdminInitializer;
import br.com.fiap.skilltrack_backend_java.skilltrack.model.Usuario;
import br.com.fiap.skilltrack_backend_java.skilltrack.model.enums.Perfil;
import br.com.fiap.skilltrack_backend_java.skilltrack.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AdminInitializerTests {

    private static final String EMAIL = "dono@email.com";
    private static final String SENHA = "senhaDoDono";

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void criaOAdminQuandoOEmailNaoExiste() {
        iniciarAplicacao();

        var admin = usuarioRepository.findByEmail(EMAIL).orElseThrow();
        assertThat(admin.getPerfil()).isEqualTo(Perfil.ADMIN);
        assertThat(passwordEncoder.matches(SENHA, admin.getSenha())).isTrue();
    }

    @Test
    void naoPromoveContaCadastradaPorOutraPessoaComOEmailDoAdmin() {
        criarUsuario(EMAIL, "senhaDeOutraPessoa", Perfil.USER);

        iniciarAplicacao();

        assertThat(usuarioRepository.findByEmail(EMAIL).orElseThrow().getPerfil()).isEqualTo(Perfil.USER);
    }

    @Test
    void promoveContaExistenteQuandoASenhaConfere() {
        criarUsuario(EMAIL, SENHA, Perfil.USER);

        iniciarAplicacao();

        assertThat(usuarioRepository.findByEmail(EMAIL).orElseThrow().getPerfil()).isEqualTo(Perfil.ADMIN);
    }

    @Test
    void adminRebaixadoNaoVoltaASerAdminAoReiniciar() {
        criarUsuario("outro@email.com", SENHA, Perfil.ADMIN);
        criarUsuario(EMAIL, SENHA, Perfil.USER);

        iniciarAplicacao();

        assertThat(usuarioRepository.findByEmail(EMAIL).orElseThrow().getPerfil()).isEqualTo(Perfil.USER);
    }

    @Test
    void adminExcluidoNaoERecriadoAoReiniciar() {
        criarUsuario("outro@email.com", SENHA, Perfil.ADMIN);

        iniciarAplicacao();

        assertThat(usuarioRepository.findByEmail(EMAIL)).isEmpty();
    }

    private void iniciarAplicacao() {
        new AdminInitializer(usuarioRepository, passwordEncoder, EMAIL, SENHA, "Administrador").run(null);
    }

    private void criarUsuario(String email, String senha, Perfil perfil) {
        var usuario = new Usuario();
        usuario.setNome("Usuário");
        usuario.setEmail(email);
        usuario.setSenha(passwordEncoder.encode(senha));
        usuario.setPerfil(perfil);
        usuarioRepository.save(usuario);
    }
}
