package br.com.fiap.skilltrack_backend_java.skilltrack.service;

import br.com.fiap.skilltrack_backend_java.skilltrack.dto.SkillRequestDTO;
import br.com.fiap.skilltrack_backend_java.skilltrack.dto.SkillResponseDTO;
import br.com.fiap.skilltrack_backend_java.skilltrack.mapper.SkillMapper;
import br.com.fiap.skilltrack_backend_java.skilltrack.model.Skill;
import br.com.fiap.skilltrack_backend_java.skilltrack.repository.SkillRepository;
import br.com.fiap.skilltrack_backend_java.skilltrack.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SkillService {

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public Page<SkillResponseDTO> listarTodos(Pageable pageable) {
        return skillRepository.findAllByOrderByIdAsc(pageable)
                .map(SkillMapper::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public SkillResponseDTO buscarPorId(Long id) {
        var skill = buscarEntidadeSkillPorId(id);
        return SkillMapper.toResponseDTO(skill);
    }

    @Transactional
    public SkillResponseDTO salvar(SkillRequestDTO dto) {
        var skill = SkillMapper.toEntity(dto);

        // Associate user
        var usuario = usuarioRepository.findById(dto.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("Usuário com id: " + dto.getUserId() + " não encontrado"));
        skill.setUser(usuario);

        var salvo = skillRepository.save(skill);
        return SkillMapper.toResponseDTO(salvo);
    }

    @Transactional
    public SkillResponseDTO atualizar(Long id, SkillRequestDTO dto) {
        var skillAtual = buscarEntidadeSkillPorId(id);

        skillAtual.setName(dto.getName());
        skillAtual.setCategory(dto.getCategory());
        skillAtual.setLevel(dto.getLevel());

        var atualizado = skillRepository.save(skillAtual);
        return SkillMapper.toResponseDTO(atualizado);
    }

    @Transactional
    public void deletar(Long id) {
        var skill = buscarEntidadeSkillPorId(id);
        skillRepository.delete(skill);
    }

    public Skill buscarEntidadeSkillPorId(Long id) {
        return skillRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Skill com id: " + id + " não encontrada"));
    }
}
