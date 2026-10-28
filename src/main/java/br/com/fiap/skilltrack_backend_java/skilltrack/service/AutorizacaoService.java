package br.com.fiap.skilltrack_backend_java.skilltrack.service;

import br.com.fiap.skilltrack_backend_java.skilltrack.model.Usuario;
import br.com.fiap.skilltrack_backend_java.skilltrack.model.enums.Perfil;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

// Centraliza as regras de acesso: cada usuário acessa apenas os próprios dados; ADMIN acessa tudo
@Service
public class AutorizacaoService {

    public Optional<Usuario> getUsuarioLogado() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Usuario usuario) {
            return Optional.of(usuario);
        }
        return Optional.empty();
    }

    public Long getIdUsuarioLogado() {
        return getUsuarioLogado()
                .map(Usuario::getId)
                .orElseThrow(() -> new AccessDeniedException("Usuário não autenticado"));
    }

    public boolean isAdmin() {
        return getUsuarioLogado()
                .map(usuario -> usuario.getPerfil() == Perfil.ADMIN)
                .orElse(false);
    }

    public void verificarDono(Long idDono) {
        if (isAdmin()) return;
        if (idDono == null || !idDono.equals(getIdUsuarioLogado())) {
            throw new AccessDeniedException("Acesso negado ao recurso de outro usuário");
        }
    }

    public void verificarDono(Usuario dono) {
        verificarDono(dono != null ? dono.getId() : null);
    }
}
