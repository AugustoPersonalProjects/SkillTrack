package br.com.fiap.skilltrack_backend_java.skilltrack.service;

import br.com.fiap.skilltrack_backend_java.skilltrack.dto.UsuarioRequestDTO;
import br.com.fiap.skilltrack_backend_java.skilltrack.dto.UsuarioResponseDTO;
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

@Service
public class UsuarioService {

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
        usuario.setSenha(passwordEncoder.encode(usuarioRequestDTO.getSenha()));
        // somente um ADMIN pode definir o perfil; cadastro público é sempre USER
        usuario.setPerfil(autorizacaoService.isAdmin() && usuarioRequestDTO.getPerfil() != null
                ? usuarioRequestDTO.getPerfil()
                : Perfil.USER);

        return usuarioRepository.save(usuario);
    }

    @Transactional
    public UsuarioResponseDTO atualizar(Long id, UsuarioRequestDTO usuarioRequestDTO) {
        var usuarioAtual = buscarEntidadeUsuarioPorId(id);
        validarEmailDisponivel(usuarioRequestDTO.getEmail(), id);

        usuarioAtual.setNome(usuarioRequestDTO.getNome());
        usuarioAtual.setEmail(usuarioRequestDTO.getEmail());
        if (usuarioRequestDTO.getSenha() != null && !usuarioRequestDTO.getSenha().isBlank()) {
            usuarioAtual.setSenha(passwordEncoder.encode(usuarioRequestDTO.getSenha()));
        }
        if (autorizacaoService.isAdmin() && usuarioRequestDTO.getPerfil() != null) {
            usuarioAtual.setPerfil(usuarioRequestDTO.getPerfil());
        }

        var usuarioAtualizado = usuarioRepository.save(usuarioAtual);
        return UsuarioMapper.toResponseDTO(usuarioAtualizado);
    }

    @Transactional
    public void deletar(Long id) {
        var usuario = buscarEntidadeUsuarioPorId(id);
        usuarioRepository.delete(usuario);
    }

    // busca o usuário e garante que é o próprio usuário logado (ou um ADMIN)
    public Usuario buscarEntidadeUsuarioPorId(Long id) {
        autorizacaoService.verificarDono(id);
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário com id: " + id + " não encontrado"));
    }

    private void validarEmailDisponivel(String email, Long idUsuarioAtual) {
        usuarioRepository.findByEmail(email)
                .filter(existente -> !existente.getId().equals(idUsuarioAtual))
                .ifPresent(existente -> {
                    throw new RegraNegocioException("E-mail já cadastrado.");
                });
    }
}
