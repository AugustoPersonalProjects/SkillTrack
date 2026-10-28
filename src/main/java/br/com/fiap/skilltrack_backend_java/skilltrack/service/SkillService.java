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

import java.util.List;

@Service
public class SkillService {

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private AutorizacaoService autorizacaoService;

    @Transactional(readOnly = true)
    public Page<SkillResponseDTO> listarTodos(Pageable pageable) {
        var skills = autorizacaoService.isAdmin()
                ? skillRepository.findAllByOrderByIdAsc(pageable)
                : skillRepository.findByUserIdOrderByIdAsc(autorizacaoService.getIdUsuarioLogado(), pageable);
        return skills.map(SkillMapper::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public SkillResponseDTO buscarPorId(Long id) {
        var skill = buscarEntidadeSkillPorId(id);
        return SkillMapper.toResponseDTO(skill);
    }

    @Transactional(readOnly = true)
    public List<SkillResponseDTO> listarPorUsuario(Long userId) {
        autorizacaoService.verificarDono(userId);
        return skillRepository.findByUserIdOrderByNameAsc(userId).stream()
                .map(SkillMapper::toResponseDTO)
                .toList();
    }

    @Transactional
    public SkillResponseDTO salvar(SkillRequestDTO dto) {
        autorizacaoService.verificarDono(dto.getUserId());

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
        skillAtual.setDescription(dto.getDescription());
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

    // busca a skill e garante que ela pertence ao usuário logado
    public Skill buscarEntidadeSkillPorId(Long id) {
        var skill = skillRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Skill com id: " + id + " não encontrada"));
        autorizacaoService.verificarDono(skill.getUser());
        return skill;
    }
}
