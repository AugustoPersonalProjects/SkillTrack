package br.com.fiap.skilltrack_backend_java.skilltrack.service;

import br.com.fiap.skilltrack_backend_java.skilltrack.dto.UsuarioRequestDTO;
import br.com.fiap.skilltrack_backend_java.skilltrack.dto.UsuarioResponseDTO;
import br.com.fiap.skilltrack_backend_java.skilltrack.dto.UsuarioUpdateDTO;
import br.com.fiap.skilltrack_backend_java.skilltrack.exceptions.RegraNegocioException;
import br.com.fiap.skilltrack_backend_java.skilltrack.mapper.UsuarioMapper;
import br.com.fiap.skilltrack_backend_java.skilltrack.model.Usuario;
import br.com.fiap.skilltrack_backend_java.skilltrack.model.enums.Perfil;
import br.com.fiap.skilltrack_backend_java.skilltrack.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.nio.charset.StandardCharsets;

@Service
public class UsuarioService {

    private static final int TAMANHO_MAXIMO_SENHA_BYTES = 72;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AutorizacaoService autorizacaoService;

    @Transactional(readOnly = true)
    public Page<UsuarioResponseDTO> listarTodos(Pageable pageable) {
        return usuarioRepository.findAllByOrderByIdAsc(pageable)
                .map(UsuarioMapper::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarPorId(Long id) {
        var usuario = buscarEntidadeUsuarioPorId(id);
        return UsuarioMapper.toResponseDTO(usuario);
    }

    @Transactional
    public UsuarioResponseDTO salvar(UsuarioRequestDTO usuarioRequestDTO) {
        return UsuarioMapper.toResponseDTO(cadastrar(usuarioRequestDTO));
    }

    @Transactional
    public Usuario cadastrar(UsuarioRequestDTO usuarioRequestDTO) {
        validarEmailDisponivel(usuarioRequestDTO.getEmail(), null);

        var usuario = UsuarioMapper.toEntity(usuarioRequestDTO);
        usuario.setSenha(codificarSenha(usuarioRequestDTO.getSenha()));
        // somente um ADMIN pode definir o perfil; cadastro público é sempre USER
        usuario.setPerfil(autorizacaoService.isAdmin() && usuarioRequestDTO.getPerfil() != null
                ? usuarioRequestDTO.getPerfil()
                : Perfil.USER);

        return usuarioRepository.save(usuario);
    }

    @Transactional
    public UsuarioResponseDTO atualizar(Long id, UsuarioUpdateDTO usuarioUpdateDTO) {
        var usuarioAtual = buscarEntidadeUsuarioPorId(id);
        validarEmailDisponivel(usuarioUpdateDTO.getEmail(), id);

        usuarioAtual.setNome(usuarioUpdateDTO.getNome());
        usuarioAtual.setEmail(usuarioUpdateDTO.getEmail());
        if (usuarioUpdateDTO.getSenha() != null && !usuarioUpdateDTO.getSenha().isBlank()) {
            definirSenha(usuarioAtual, usuarioUpdateDTO.getSenha());
        }
        var novoPerfil = usuarioUpdateDTO.getPerfil();
        if (autorizacaoService.isAdmin() && novoPerfil != null && novoPerfil != usuarioAtual.getPerfil()) {
            if (novoPerfil != Perfil.ADMIN) {
                garantirOutroAdministrador(usuarioAtual);
            }
            usuarioAtual.setPerfil(novoPerfil);
        }

        var usuarioAtualizado = usuarioRepository.save(usuarioAtual);
        return UsuarioMapper.toResponseDTO(usuarioAtualizado);
    }

    @Transactional
    public void deletar(Long id) {
        var usuario = buscarEntidadeUsuarioPorId(id);
        garantirOutroAdministrador(usuario);
        usuarioRepository.delete(usuario);
    }

    // busca o usuário e garante que é o próprio usuário logado (ou um ADMIN)
    public Usuario buscarEntidadeUsuarioPorId(Long id) {
        autorizacaoService.verificarDono(id);
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário com id: " + id + " não encontrado"));
    }

    // trocar a senha encerra as sessões abertas, inclusive a de quem roubou um token
    private void definirSenha(Usuario usuario, String novaSenha) {
        usuario.setSenha(codificarSenha(novaSenha));
        usuario.setVersaoToken(usuario.getVersaoToken() + 1);
    }

    // o limite do BCrypt é de 72 bytes, e acentos e emojis ocupam mais de um byte por caractere
    private String codificarSenha(String senha) {
        if (senha.getBytes(StandardCharsets.UTF_8).length > TAMANHO_MAXIMO_SENHA_BYTES) {
            throw new RegraNegocioException("A senha é longa demais. Use menos caracteres, principalmente acentos e emojis.");
        }
        return passwordEncoder.encode(senha);
    }

    // o sistema não pode ficar sem nenhum ADMIN, senão ninguém mais gerencia usuários e categorias
    private void garantirOutroAdministrador(Usuario usuario) {
        if (usuario.getPerfil() == Perfil.ADMIN && usuarioRepository.countByPerfil(Perfil.ADMIN) <= 1) {
            throw new RegraNegocioException("Não é possível remover o último administrador do sistema.");
        }
    }

    private void validarEmailDisponivel(String email, Long idUsuarioAtual) {
        usuarioRepository.findByEmail(email)
                .filter(existente -> !existente.getId().equals(idUsuarioAtual))
                .ifPresent(existente -> {
                    throw new RegraNegocioException("E-mail já cadastrado.");
                });
    }
}
