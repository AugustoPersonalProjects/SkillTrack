package br.com.fiap.skilltrack_backend_java.skilltrack.controller;

import br.com.fiap.skilltrack_backend_java.skilltrack.dto.AutenticacaoDTO;
import br.com.fiap.skilltrack_backend_java.skilltrack.dto.TokenDTO;
import br.com.fiap.skilltrack_backend_java.skilltrack.model.Usuario;
import br.com.fiap.skilltrack_backend_java.skilltrack.model.enums.Perfil;
import br.com.fiap.skilltrack_backend_java.skilltrack.repository.UsuarioRepository;
import br.com.fiap.skilltrack_backend_java.skilltrack.service.TokenService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager manager;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // 🔹 LOGIN EXISTENTE
    @PostMapping("/login")
    public ResponseEntity<TokenDTO> efetuarLogin(@RequestBody @Valid AutenticacaoDTO dados) {
        var authenticationToken = new UsernamePasswordAuthenticationToken(dados.email(), dados.senha());
        var authentication = manager.authenticate(authenticationToken);

        var tokenJWT = tokenService.gerarToken((Usuario) authentication.getPrincipal());
        return ResponseEntity.ok(new TokenDTO(tokenJWT));
    }

    // 🔹 NOVO ENDPOINT DE REGISTRO
    @PostMapping("/register")
    public ResponseEntity<?> registrar(@RequestBody @Valid Usuario novoUsuario) {
        try {
            if (usuarioRepository.findByEmail(novoUsuario.getEmail()).isPresent()) {
                return ResponseEntity.badRequest().body("E-mail já cadastrado.");
            }

            novoUsuario.setSenha(passwordEncoder.encode(novoUsuario.getSenha()));

            // Define perfil padrão se não vier no body
            if (novoUsuario.getPerfil() == null) {
                novoUsuario.setPerfil(Perfil.USER);
            }

            usuarioRepository.save(novoUsuario);

            var tokenJWT = tokenService.gerarToken(novoUsuario);
            return ResponseEntity.ok(new TokenDTO(tokenJWT));

        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.badRequest().body("Erro: e-mail já cadastrado ou dados inválidos.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erro ao registrar usuário: " + e.getMessage());
        }
    }
}
