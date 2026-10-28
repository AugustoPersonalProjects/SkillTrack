package br.com.fiap.skilltrack_backend_java.skilltrack.service;

import br.com.fiap.skilltrack_backend_java.skilltrack.dto.GoalRequestDTO;
import br.com.fiap.skilltrack_backend_java.skilltrack.dto.GoalResponseDTO;
import br.com.fiap.skilltrack_backend_java.skilltrack.mapper.GoalMapper;
import br.com.fiap.skilltrack_backend_java.skilltrack.model.Goal;
import br.com.fiap.skilltrack_backend_java.skilltrack.repository.GoalRepository;
import br.com.fiap.skilltrack_backend_java.skilltrack.repository.SkillRepository;
import br.com.fiap.skilltrack_backend_java.skilltrack.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class GoalService {

    @Autowired
    private GoalRepository goalRepository;

    @Autowired
    private SkillRepository skillRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AutorizacaoService autorizacaoService;

    @Transactional(readOnly = true)
    public Page<GoalResponseDTO> listarTodos(Pageable pageable) {
        var goals = autorizacaoService.isAdmin()
                ? goalRepository.findAllByOrderByIdAsc(pageable)
                : goalRepository.findByUserIdOrderByIdAsc(autorizacaoService.getIdUsuarioLogado(), pageable);
        return goals.map(GoalMapper::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public GoalResponseDTO buscarPorId(Long id) {
        return GoalMapper.toResponseDTO(buscarEntidadeGoalPorId(id));
    }

    @Transactional
    public GoalResponseDTO salvar(GoalRequestDTO dto) {
        // valida deadline
        if (dto.getDeadline() != null && dto.getDeadline().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Deadline deve ser uma data futura");
        }

        autorizacaoService.verificarDono(dto.getUserId());

        var skill = skillRepository.findById(dto.getSkillId())
                .orElseThrow(() -> new EntityNotFoundException("Skill com id: " + dto.getSkillId() + " não encontrada"));
        autorizacaoService.verificarDono(skill.getUser());

        var usuario = usuarioRepository.findById(dto.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("Usuário com id: " + dto.getUserId() + " não encontrado"));

        Goal goal = GoalMapper.toEntity(dto, skill, usuario);

        var salvo = goalRepository.save(goal);
        return GoalMapper.toResponseDTO(salvo);
    }

    @Transactional
    public GoalResponseDTO atualizar(Long id, GoalRequestDTO dto) {
        var goalAtual = buscarEntidadeGoalPorId(id);

        if (dto.getDeadline() != null && dto.getDeadline().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Deadline deve ser uma data futura");
        }

        goalAtual.setTitle(dto.getTituloEfetivo());
        goalAtual.setDescription(dto.getDescricaoEfetiva());
        goalAtual.setDeadline(dto.getDeadline());

        var usuarioAtualId = goalAtual.getUser() != null ? goalAtual.getUser().getId() : null;
        if (dto.getUserId() != null && !dto.getUserId().equals(usuarioAtualId)) {
            autorizacaoService.verificarDono(dto.getUserId());
            var novoUsuario = usuarioRepository.findById(dto.getUserId())
                    .orElseThrow(() -> new EntityNotFoundException("Usuário com id: " + dto.getUserId() + " não encontrado"));
            goalAtual.setUser(novoUsuario);
        }

        var skillAtualId = goalAtual.getSkill() != null ? goalAtual.getSkill().getId() : null;
        if (dto.getSkillId() != null && !dto.getSkillId().equals(skillAtualId)) {
            var novaSkill = skillRepository.findById(dto.getSkillId())
                    .orElseThrow(() -> new EntityNotFoundException("Skill com id: " + dto.getSkillId() + " não encontrada"));
            autorizacaoService.verificarDono(novaSkill.getUser());
            goalAtual.setSkill(novaSkill);
        }

        var atualizado = goalRepository.save(goalAtual);
        return GoalMapper.toResponseDTO(atualizado);
    }

    @Transactional(readOnly = true)
    public List<GoalResponseDTO> buscarPorUsuario(Long userId) {
        autorizacaoService.verificarDono(userId);
        return goalRepository.findByUserId(userId).stream()
                .map(GoalMapper::toResponseDTO)
                .toList();
    }

    @Transactional
    public void deletar(Long id) {
        goalRepository.delete(buscarEntidadeGoalPorId(id));
    }

    @Transactional
    public void marcarComoCompleta(Long id) {
        var goal = buscarEntidadeGoalPorId(id);
        goal.setCompleted(true);
        goalRepository.save(goal);
    }

    // busca a meta e garante que ela pertence ao usuário logado
    private Goal buscarEntidadeGoalPorId(Long id) {
        var goal = goalRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Goal com id: " + id + " não encontrada"));
        var dono = goal.getUser() != null ? goal.getUser()
                : goal.getSkill() != null ? goal.getSkill().getUser() : null;
        autorizacaoService.verificarDono(dono);
        return goal;
    }
}
