package br.com.fiap.skilltrack_backend_java.skilltrack.security;

import br.com.fiap.skilltrack_backend_java.skilltrack.repository.UsuarioRepository;
import br.com.fiap.skilltrack_backend_java.skilltrack.service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UsuarioRepository repository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        var tokenJWT = recuperarToken(request);
        // token inválido/expirado não deve derrubar a requisição: segue sem autenticação
        // (rotas públicas como /auth/login continuam funcionando; as protegidas retornam 401)
        var dadosToken = tokenJWT != null ? tokenService.lerTokenOuNull(tokenJWT) : null;
        if (dadosToken != null) {
            // a versão muda quando a senha é trocada, e os tokens emitidos antes da troca deixam de valer
            repository.findById(dadosToken.idUsuario())
                    .filter(usuario -> usuario.getVersaoToken() == dadosToken.versao())
                    .ifPresent(usuario -> {
                        var authentication = new UsernamePasswordAuthenticationToken(
                                usuario,
                                null,
                                usuario.getAuthorities()
                        );

                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    });
        }

        filterChain.doFilter(request, response);
    }

    private String recuperarToken(HttpServletRequest request) {
        var authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.replace("Bearer ", "");
        }

        return null;
    }
}
