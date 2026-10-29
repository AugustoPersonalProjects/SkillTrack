package br.com.fiap.skilltrack_backend_java.skilltrack.controller;

import br.com.fiap.skilltrack_backend_java.skilltrack.mapper.GoalMapper;
import br.com.fiap.skilltrack_backend_java.skilltrack.mapper.ProgressMapper;
import br.com.fiap.skilltrack_backend_java.skilltrack.model.Progress;
import br.com.fiap.skilltrack_backend_java.skilltrack.model.Skill;
import br.com.fiap.skilltrack_backend_java.skilltrack.repository.*;
import br.com.fiap.skilltrack_backend_java.skilltrack.service.AutorizacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

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

        // O progresso atual de cada skill é o seu último registro (0 quando ainda não há registros)
        var skills = skillRepository.findByUserIdOrderByNameAsc(userId);
        var latestBySkill = progressRepository.findLatestBySkillUserId(userId).stream()
                .collect(Collectors.toMap(p -> p.getSkill().getId(), Progress::getPercentage));
        var skillProgressValues = skills.stream()
                .map(skill -> latestBySkill.getOrDefault(skill.getId(), 0).doubleValue())
                .toList();

        // Estatísticas principais do usuário logado; a média considera todas as skills
        dashboard.put("totalSkills", skills.size());
        dashboard.put("totalGoals", goalRepository.countByUserId(userId));
        dashboard.put("averageProgress", skillProgressValues.stream().mapToDouble(Double::doubleValue).average().orElse(0));

        // Dados para o gráfico (listas alinhadas pelo mesmo índice)
        dashboard.put("skillNames", skills.stream().map(Skill::getName).toList());
        dashboard.put("skillProgressValues", skillProgressValues);

        // Últimas metas e progressos
        dashboard.put("goals", goalRepository.findTop5ByUserIdOrderByDeadlineAsc(userId).stream().map(GoalMapper::toResponseDTO).toList());
        dashboard.put("progressList", progressRepository.findTop5BySkillUserIdOrderByDateDescIdDesc(userId).stream().map(ProgressMapper::toResponseDTO).toList());

        return ResponseEntity.ok(dashboard);
    }
}
