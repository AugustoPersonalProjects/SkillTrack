package br.com.fiap.skilltrack_backend_java.skilltrack.controller;

import br.com.fiap.skilltrack_backend_java.skilltrack.dto.AutenticacaoDTO;
import br.com.fiap.skilltrack_backend_java.skilltrack.dto.TokenDTO;
import br.com.fiap.skilltrack_backend_java.skilltrack.dto.UsuarioRequestDTO;
import br.com.fiap.skilltrack_backend_java.skilltrack.model.Usuario;
import br.com.fiap.skilltrack_backend_java.skilltrack.service.TokenService;
import br.com.fiap.skilltrack_backend_java.skilltrack.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager manager;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping("/login")
    public ResponseEntity<TokenDTO> efetuarLogin(@RequestBody @Valid AutenticacaoDTO dados) {
        var authenticationToken = new UsernamePasswordAuthenticationToken(dados.email(), dados.senha());
        var authentication = manager.authenticate(authenticationToken);

        var tokenJWT = tokenService.gerarToken((Usuario) authentication.getPrincipal());
        return ResponseEntity.ok(new TokenDTO(tokenJWT));
    }

    // cadastro + login em um passo: retorna o token do novo usuário
    @PostMapping("/register")
    public ResponseEntity<TokenDTO> registrar(@RequestBody @Valid UsuarioRequestDTO dados) {
        var novoUsuario = usuarioService.cadastrar(dados);
        var tokenJWT = tokenService.gerarToken(novoUsuario);
        return ResponseEntity.ok(new TokenDTO(tokenJWT));
    }
}
