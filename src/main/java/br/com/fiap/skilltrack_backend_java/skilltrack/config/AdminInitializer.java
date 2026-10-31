package br.com.fiap.skilltrack_backend_java.skilltrack.config;

import br.com.fiap.skilltrack_backend_java.skilltrack.model.Usuario;
import br.com.fiap.skilltrack_backend_java.skilltrack.model.enums.Perfil;
import br.com.fiap.skilltrack_backend_java.skilltrack.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// Cria o primeiro ADMIN a partir das variáveis ADMIN_EMAIL e ADMIN_PASSWORD.
// Só age enquanto o sistema não tem nenhum ADMIN: depois disso, os perfis são geridos pelo painel.
@Component
public class AdminInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInitializer.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String senha;
    private final String nome;

    public AdminInitializer(UsuarioRepository usuarioRepository,
                            PasswordEncoder passwordEncoder,
                            @Value("${api.admin.email:}") String email,
                            @Value("${api.admin.password:}") String senha,
                            @Value("${api.admin.name:Administrador}") String nome) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.email = email;
        this.senha = senha;
        this.nome = nome;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (email.isBlank() || usuarioRepository.countByPerfil(Perfil.ADMIN) > 0) return;

        usuarioRepository.findByEmail(email).ifPresentOrElse(usuario -> {
            // o cadastro público não confirma o e-mail: a conta só é promovida se a senha também conferir
            if (!passwordEncoder.matches(senha, usuario.getSenha())) {
                log.warn("Já existe uma conta com o e-mail {} e a senha dela não é ADMIN_PASSWORD; ela não foi promovida", email);
                return;
            }
            usuario.setPerfil(Perfil.ADMIN);
            usuarioRepository.save(usuario);
            log.info("Usuário {} promovido a ADMIN", email);
        }, () -> {
            if (senha.length() < 6) {
                log.warn("ADMIN_PASSWORD ausente ou com menos de 6 caracteres; o ADMIN {} não foi criado", email);
                return;
            }
            var admin = new Usuario();
            admin.setNome(nome);
            admin.setEmail(email);
            admin.setSenha(passwordEncoder.encode(senha));
            admin.setPerfil(Perfil.ADMIN);
            usuarioRepository.save(admin);
            log.info("ADMIN {} criado", email);
        });
    }
}
