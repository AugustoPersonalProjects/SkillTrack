package br.com.fiap.skilltrack_backend_java.skilltrack.service;

import br.com.fiap.skilltrack_backend_java.skilltrack.dto.AdminResumoDTO;
import br.com.fiap.skilltrack_backend_java.skilltrack.model.enums.Perfil;
import br.com.fiap.skilltrack_backend_java.skilltrack.repository.CategoryRepository;
import br.com.fiap.skilltrack_backend_java.skilltrack.repository.GoalRepository;
import br.com.fiap.skilltrack_backend_java.skilltrack.repository.ProgressRepository;
import br.com.fiap.skilltrack_backend_java.skilltrack.repository.SkillRepository;
import br.com.fiap.skilltrack_backend_java.skilltrack.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AdminService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private GoalRepository goalRepository;

    @Autowired
    private ProgressRepository progressRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public AdminResumoDTO buscarResumo() {
        // contagens por usuário, em uma consulta por tabela
        var skillsPorUsuario = skillRepository.countGroupedByUser().stream()
                .collect(Collectors.toMap(SkillRepository.TotalPorUsuario::getIdUsuario, SkillRepository.TotalPorUsuario::getTotal));
        var metasPorUsuario = goalRepository.countGroupedByUser().stream()
                .collect(Collectors.toMap(GoalRepository.TotalPorUsuario::getIdUsuario, Function.identity()));

        var usuarios = usuarioRepository.findAll(Sort.by("id")).stream()
                .map(usuario -> {
                    var metas = metasPorUsuario.get(usuario.getId());
                    return new AdminResumoDTO.Usuario(
                            usuario.getId(),
                            usuario.getNome(),
                            usuario.getEmail(),
                            usuario.getPerfil(),
                            usuario.getDataCriacao(),
                            usuario.getCorAvatar(),
                            skillsPorUsuario.getOrDefault(usuario.getId(), 0L),
                            metas != null ? metas.getTotal() : 0,
                            metas != null ? metas.getConcluidas() : 0
                    );
                })
                .toList();

        return new AdminResumoDTO(
                usuarioRepository.count(),
                usuarioRepository.countByPerfil(Perfil.ADMIN),
                skillRepository.count(),
                goalRepository.count(),
                goalRepository.countByCompletedTrue(),
                progressRepository.count(),
                categoryRepository.count(),
                usuarios
        );
    }
}
