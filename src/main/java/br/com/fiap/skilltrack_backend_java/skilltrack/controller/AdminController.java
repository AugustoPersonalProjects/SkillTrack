package br.com.fiap.skilltrack_backend_java.skilltrack.controller;

import br.com.fiap.skilltrack_backend_java.skilltrack.model.enums.Perfil;
import br.com.fiap.skilltrack_backend_java.skilltrack.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// Visão geral do sistema para o painel de administração
@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

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

    @GetMapping("/resumo")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> resumo() {
        var resumo = new LinkedHashMap<String, Object>();

        resumo.put("totalUsuarios", usuarioRepository.count());
        resumo.put("totalAdmins", usuarioRepository.countByPerfil(Perfil.ADMIN));
        resumo.put("totalSkills", skillRepository.count());
        resumo.put("totalGoals", goalRepository.count());
        resumo.put("completedGoals", goalRepository.countByCompletedTrue());
        resumo.put("totalProgress", progressRepository.count());
        resumo.put("totalCategories", categoryRepository.count());

        // contagens por usuário, em uma consulta por tabela
        Map<Long, Long> skillsPorUsuario = skillRepository.countGroupedByUser().stream()
                .collect(Collectors.toMap(row -> (Long) row[0], row -> ((Number) row[1]).longValue()));
        Map<Long, long[]> metasPorUsuario = goalRepository.countGroupedByUser().stream()
                .collect(Collectors.toMap(row -> (Long) row[0],
                        row -> new long[]{((Number) row[1]).longValue(), ((Number) row[2]).longValue()}));

        List<Map<String, Object>> usuarios = usuarioRepository.findAll().stream()
                .sorted((a, b) -> a.getId().compareTo(b.getId()))
                .map(usuario -> {
                    var metas = metasPorUsuario.getOrDefault(usuario.getId(), new long[]{0, 0});
                    var linha = new LinkedHashMap<String, Object>();
                    linha.put("id_usuario", usuario.getId());
                    linha.put("nome", usuario.getNome());
                    linha.put("email", usuario.getEmail());
                    linha.put("perfil", usuario.getPerfil());
                    linha.put("data_criacao", usuario.getDataCriacao());
                    linha.put("totalSkills", skillsPorUsuario.getOrDefault(usuario.getId(), 0L));
                    linha.put("totalGoals", metas[0]);
                    linha.put("completedGoals", metas[1]);
                    return (Map<String, Object>) linha;
                })
                .toList();
        resumo.put("usuarios", usuarios);

        return ResponseEntity.ok(resumo);
    }
}
