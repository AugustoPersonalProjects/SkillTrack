package br.com.fiap.skilltrack_backend_java.skilltrack.service;

import br.com.fiap.skilltrack_backend_java.skilltrack.dto.AlteracaoEmailDTO;
import br.com.fiap.skilltrack_backend_java.skilltrack.dto.AlteracaoSenhaDTO;
import br.com.fiap.skilltrack_backend_java.skilltrack.dto.AlteracaoTemaDTO;
import br.com.fiap.skilltrack_backend_java.skilltrack.dto.DadosPessoaisDTO;
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
import org.springframework.util.StringUtils;
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
        var informouSenha = usuarioUpdateDTO.getSenha() != null && !usuarioUpdateDTO.getSenha().isBlank();
        // as próprias credenciais só mudam pelas rotas /usuarios/me, que confirmam a senha atual
        if (id.equals(autorizacaoService.getIdUsuarioLogado())
                && (informouSenha || !usuarioAtual.getEmail().equalsIgnoreCase(usuarioUpdateDTO.getEmail()))) {
            throw new RegraNegocioException("Altere o seu e-mail e a sua senha na página Meu perfil.");
        }
        validarEmailDisponivel(usuarioUpdateDTO.getEmail(), id);

        usuarioAtual.setNome(usuarioUpdateDTO.getNome());
        usuarioAtual.setEmail(usuarioUpdateDTO.getEmail());
        if (informouSenha) {
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

    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarUsuarioLogado() {
        return UsuarioMapper.toResponseDTO(buscarEntidadeUsuarioLogado());
    }

    @Transactional
    public UsuarioResponseDTO atualizarDadosPessoais(DadosPessoaisDTO dados) {
        var usuario = buscarEntidadeUsuarioLogado();

        usuario.setNome(dados.nome().trim());
        usuario.setBio(StringUtils.hasText(dados.bio()) ? dados.bio().trim() : null);
        usuario.setCorAvatar(dados.corAvatar());
        return UsuarioMapper.toResponseDTO(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponseDTO alterarTema(AlteracaoTemaDTO dados) {
        var usuario = buscarEntidadeUsuarioLogado();

        usuario.setTema(dados.tema());
        return UsuarioMapper.toResponseDTO(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponseDTO alterarEmail(AlteracaoEmailDTO dados) {
        var usuario = buscarEntidadeUsuarioLogado();
        confirmarSenhaAtual(usuario, dados.senhaAtual());
        validarEmailDisponivel(dados.email(), usuario.getId());

        usuario.setEmail(dados.email());
        return UsuarioMapper.toResponseDTO(usuarioRepository.save(usuario));
    }

    @Transactional
    public Usuario alterarSenha(AlteracaoSenhaDTO dados) {
        var usuario = buscarEntidadeUsuarioLogado();
        confirmarSenhaAtual(usuario, dados.senhaAtual());

        definirSenha(usuario, dados.novaSenha());
        return usuarioRepository.save(usuario);
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

    private Usuario buscarEntidadeUsuarioLogado() {
        return buscarEntidadeUsuarioPorId(autorizacaoService.getIdUsuarioLogado());
    }

    // um token roubado não basta para tomar a conta: quem altera as credenciais precisa saber a senha.
    // O erro não pode ser 401, porque o front encerraria a sessão de quem só digitou a senha errada
    private void confirmarSenhaAtual(Usuario usuario, String senhaAtual) {
        if (!passwordEncoder.matches(senhaAtual, usuario.getSenha())) {
            throw new RegraNegocioException("A senha atual está incorreta.");
        }
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
