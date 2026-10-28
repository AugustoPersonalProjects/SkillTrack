package br.com.fiap.skilltrack_backend_java.skilltrack.service;

import br.com.fiap.skilltrack_backend_java.skilltrack.dto.ProgressRequestDTO;
import br.com.fiap.skilltrack_backend_java.skilltrack.dto.ProgressResponseDTO;
import br.com.fiap.skilltrack_backend_java.skilltrack.mapper.ProgressMapper;
import br.com.fiap.skilltrack_backend_java.skilltrack.model.Progress;
import br.com.fiap.skilltrack_backend_java.skilltrack.repository.ProgressRepository;
import br.com.fiap.skilltrack_backend_java.skilltrack.repository.SkillRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProgressService {

    @Autowired
    private ProgressRepository progressRepository;

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private AutorizacaoService autorizacaoService;

    @Transactional(readOnly = true)
    public Page<ProgressResponseDTO> listarTodos(Pageable pageable) {
        var progressos = autorizacaoService.isAdmin()
                ? progressRepository.findAllByOrderByIdAsc(pageable)
                : progressRepository.findBySkillUserIdOrderByIdAsc(autorizacaoService.getIdUsuarioLogado(), pageable);
        return progressos.map(ProgressMapper::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public ProgressResponseDTO buscarPorId(Long id) {
        return ProgressMapper.toResponseDTO(buscarEntidadeProgressPorId(id));
    }

    @Transactional
    public ProgressResponseDTO salvar(ProgressRequestDTO dto) {
        if (dto.getPercentage() < 0 || dto.getPercentage() > 100) {
            throw new IllegalArgumentException("Porcentagem precisa estar entre 0 e 100");
        }

        var skill = skillRepository.findById(dto.getSkillId())
                .orElseThrow(() -> new EntityNotFoundException("Skill com id: " + dto.getSkillId() + " não encontrada"));
        autorizacaoService.verificarDono(skill.getUser());

        Progress progress = ProgressMapper.toEntity(dto, skill);
        var salvo = progressRepository.save(progress);
        return ProgressMapper.toResponseDTO(salvo);
    }

    @Transactional
    public ProgressResponseDTO atualizar(Long id, ProgressRequestDTO dto) {
        var existente = buscarEntidadeProgressPorId(id);

        if (dto.getPercentage() < 0 || dto.getPercentage() > 100) {
            throw new IllegalArgumentException("Porcentagem precisa estar entre 0 e 100");
        }

        existente.setPercentage(dto.getPercentage());
        var atualizado = progressRepository.save(existente);
        return ProgressMapper.toResponseDTO(atualizado);
    }

    @Transactional
    public void deletar(Long id) {
        progressRepository.delete(buscarEntidadeProgressPorId(id));
    }

    // busca o progresso e garante que a skill dele pertence ao usuário logado
    private Progress buscarEntidadeProgressPorId(Long id) {
        var progress = progressRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Progress com id: " + id + " não encontrado"));
        autorizacaoService.verificarDono(progress.getSkill() != null ? progress.getSkill().getUser() : null);
        return progress;
    }
}
