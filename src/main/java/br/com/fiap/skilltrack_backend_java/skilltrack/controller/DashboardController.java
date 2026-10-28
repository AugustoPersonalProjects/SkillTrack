package br.com.fiap.skilltrack_backend_java.skilltrack.controller;

import br.com.fiap.skilltrack_backend_java.skilltrack.mapper.GoalMapper;
import br.com.fiap.skilltrack_backend_java.skilltrack.mapper.ProgressMapper;
import br.com.fiap.skilltrack_backend_java.skilltrack.repository.*;
import br.com.fiap.skilltrack_backend_java.skilltrack.service.AutorizacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

// O front é separado, então o dashboard é exposto como JSON (antes retornava uma view inexistente)
@RestController
public class DashboardController {

    @Autowired
    private SkillRepository skillRepository;
    @Autowired
    private GoalRepository goalRepository;
    @Autowired
    private ProgressRepository progressRepository;
    @Autowired
    private AutorizacaoService autorizacaoService;

    @GetMapping("/dashboard")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> showDashboard() {
        var userId = autorizacaoService.getIdUsuarioLogado();
        var dashboard = new LinkedHashMap<String, Object>();

        // Estatísticas principais do usuário logado
        Double averageProgress = progressRepository.findAverageProgressByUserId(userId);
        dashboard.put("totalSkills", skillRepository.countByUserId(userId));
        dashboard.put("totalGoals", goalRepository.countByUserId(userId));
        dashboard.put("averageProgress", averageProgress != null ? averageProgress : 0);

        // Dados para o gráfico (listas alinhadas pelo mesmo índice)
        var progressBySkill = progressRepository.findAverageProgressBySkillByUserId(userId);
        dashboard.put("skillNames", progressBySkill.stream().map(row -> row[0]).toList());
        dashboard.put("skillProgressValues", progressBySkill.stream().map(row -> ((Number) row[1]).doubleValue()).toList());

        // Últimas metas e progressos
        dashboard.put("goals", goalRepository.findTop5ByUserIdOrderByDeadlineAsc(userId).stream().map(GoalMapper::toResponseDTO).toList());
        dashboard.put("progressList", progressRepository.findTop5BySkillUserIdOrderByDateDesc(userId).stream().map(ProgressMapper::toResponseDTO).toList());

        return ResponseEntity.ok(dashboard);
    }
}
