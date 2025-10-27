package br.com.fiap.skilltrack_backend_java.skilltrack.controller;

import br.com.fiap.skilltrack_backend_java.skilltrack.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @Autowired
    private SkillRepository skillRepository;
    @Autowired
    private GoalRepository goalRepository;
    @Autowired
    private ProgressRepository progressRepository;

    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        // Estatísticas principais
        long totalSkills = skillRepository.count();
        long totalGoals = goalRepository.count();
        Double averageProgress = progressRepository.findAverageProgress();

        model.addAttribute("totalSkills", totalSkills);
        model.addAttribute("totalGoals", totalGoals);
        model.addAttribute("averageProgress", averageProgress != null ? averageProgress : 0);

        // Dados para o gráfico
        model.addAttribute("skillNames", skillRepository.findSkillNames());
        model.addAttribute("skillProgressValues", progressRepository.findProgressBySkill());

        // Últimas metas e progressos
        model.addAttribute("goals", goalRepository.findTop5ByOrderByDeadlineAsc());
        model.addAttribute("progressList", progressRepository.findTop5ByOrderByDateDesc());

        return "dashboard";
    }
}
