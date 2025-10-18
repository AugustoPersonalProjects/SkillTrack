package br.com.fiap.skilltrack_backend_java.skilltrack.service;

import br.com.fiap.skilltrack_backend_java.skilltrack.dto.SkillRequestDTO;
import br.com.fiap.skilltrack_backend_java.skilltrack.dto.SkillResponseDTO;
import br.com.fiap.skilltrack_backend_java.skilltrack.mapper.SkillMapper;
import br.com.fiap.skilltrack_backend_java.skilltrack.model.Skill;
import br.com.fiap.skilltrack_backend_java.skilltrack.model.Category;
import br.com.fiap.skilltrack_backend_java.skilltrack.repository.SkillRepository;
import br.com.fiap.skilltrack_backend_java.skilltrack.repository.UsuarioRepository;
import br.com.fiap.skilltrack_backend_java.skilltrack.repository.CategoryRepository;
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

    @Autowired
    private CategoryRepository categoryRepository;

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
        // Buscar categoria pelo ID informado
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Categoria com id: " + dto.getCategoryId() + " não encontrada"));

        // Converter DTO → Entidade
        var skill = SkillMapper.toEntity(dto, category);

        // Associar usuário
        var usuario = usuarioRepository.findById(dto.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("Usuário com id: " + dto.getUserId() + " não encontrado"));
        skill.setUser(usuario);

        var salvo = skillRepository.save(skill);
        return SkillMapper.toResponseDTO(salvo);
    }

    @Transactional
    public SkillResponseDTO atualizar(Long id, SkillRequestDTO dto) {
        var skillAtual = buscarEntidadeSkillPorId(id);

        // Atualiza os campos simples
        skillAtual.setName(dto.getName());
        skillAtual.setLevel(dto.getLevel());

        // Atualiza a categoria
        Category novaCategoria = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Categoria com id: " + dto.getCategoryId() + " não encontrada"));
        skillAtual.setCategory(novaCategoria);

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
