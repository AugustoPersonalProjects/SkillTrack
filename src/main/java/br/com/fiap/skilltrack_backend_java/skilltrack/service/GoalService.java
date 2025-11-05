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
import java.util.stream.Collectors;

@Service
public class GoalService {

    @Autowired
    private GoalRepository goalRepository;

    @Autowired
    private SkillRepository skillRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public Page<GoalResponseDTO> listarTodos(Pageable pageable) {
        return goalRepository.findAllByOrderByIdAsc(pageable)
                .map(GoalMapper::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public GoalResponseDTO buscarPorId(Long id) {
        var goal = goalRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Goal com id: " + id + " não encontrada"));
        return GoalMapper.toResponseDTO(goal);
    }

    @Transactional
    public GoalResponseDTO salvar(GoalRequestDTO dto) {
        // valida deadline
        if (dto.getDeadline() != null && dto.getDeadline().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Deadline deve ser uma data futura");
        }

        var skill = skillRepository.findById(dto.getSkillId())
                .orElseThrow(() -> new EntityNotFoundException("Skill com id: " + dto.getSkillId() + " não encontrada"));

        var usuario = usuarioRepository.findById(dto.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("Usuário com id: " + dto.getUserId() + " não encontrado"));

        Goal goal = GoalMapper.toEntity(dto, skill, usuario);

        var salvo = goalRepository.save(goal);
        return GoalMapper.toResponseDTO(salvo);
    }

    @Transactional
    public GoalResponseDTO atualizar(Long id, GoalRequestDTO dto) {
        var goalAtual = goalRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Goal com id: " + id + " não encontrada"));

        if (dto.getDeadline() != null && dto.getDeadline().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Deadline deve ser uma data futura");
        }

        goalAtual.setTitle(dto.getDescription());
        goalAtual.setDescription(dto.getDescription());
        goalAtual.setDeadline(dto.getDeadline());

        if (dto.getUserId() != null && !dto.getUserId().equals(goalAtual.getUser().getId())) {
            var novoUsuario = usuarioRepository.findById(dto.getUserId())
                    .orElseThrow(() -> new EntityNotFoundException("Usuário com id: " + dto.getUserId() + " não encontrado"));
            goalAtual.setUser(novoUsuario);
        }

        var atualizado = goalRepository.save(goalAtual);
        return GoalMapper.toResponseDTO(atualizado);
    }

    @Transactional(readOnly = true)
    public List<GoalResponseDTO> buscarPorUsuario(Long userId) {
        System.out.println("Buscando metas no banco para usuário: " + userId);

        var goals = goalRepository.findByUserId(userId);
        System.out.println("Metas encontradas no banco: " + goals.size());

        var result = goals.stream()
                .map(GoalMapper::toResponseDTO)
                .collect(Collectors.toList());

        System.out.println("Metas convertidas para DTO: " + result.size());
        return result;
    }

    @Transactional
    public void deletar(Long id) {
        var goal = goalRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Goal com id: " + id + " não encontrada"));
        goalRepository.delete(goal);
    }

    @Transactional
    public void marcarComoCompleta(Long id) {
        var goal = goalRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Goal com id: " + id + " não encontrada"));
        goal.setCompleted(true);
        goalRepository.save(goal);
    }
}
