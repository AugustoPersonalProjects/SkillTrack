package br.com.fiap.skilltrack_backend_java.skilltrack.service;

import br.com.fiap.skilltrack_backend_java.skilltrack.dto.GoalResponseDTO;
import br.com.fiap.skilltrack_backend_java.skilltrack.mapper.GoalMapper;
import br.com.fiap.skilltrack_backend_java.skilltrack.model.Goal;
import br.com.fiap.skilltrack_backend_java.skilltrack.repository.GoalRepository;
import br.com.fiap.skilltrack_backend_java.skilltrack.repository.SkillRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class GoalService {

    @Autowired
    private GoalRepository goalRepository;

    @Autowired
    private SkillRepository skillRepository;

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
    public GoalResponseDTO salvar(com.skilltrack.dto.GoalRequestDTO dto) {
        // valida deadline
        if (dto.getDeadline() != null && dto.getDeadline().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Deadline deve ser uma data futura");
        }

        var skill = skillRepository.findById(dto.getSkillId())
                .orElseThrow(() -> new EntityNotFoundException("Skill com id: " + dto.getSkillId() + " não encontrada"));

        Goal goal = GoalMapper.toEntity(dto, skill);
        var salvo = goalRepository.save(goal);
        return GoalMapper.toResponseDTO(salvo);
    }

    @Transactional
    public GoalResponseDTO atualizar(Long id, com.skilltrack.dto.GoalRequestDTO dto) {
        var goalAtual = goalRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Goal com id: " + id + " não encontrada"));

        if (dto.getDeadline() != null && dto.getDeadline().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Deadline deve ser uma data futura");
        }

        goalAtual.setDescription(dto.getDescription());
        goalAtual.setDeadline(dto.getDeadline());

        var atualizado = goalRepository.save(goalAtual);
        return GoalMapper.toResponseDTO(atualizado);
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
