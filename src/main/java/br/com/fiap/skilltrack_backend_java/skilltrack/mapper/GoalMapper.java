package br.com.fiap.skilltrack_backend_java.skilltrack.mapper;

import br.com.fiap.skilltrack_backend_java.skilltrack.dto.GoalRequestDTO;
import br.com.fiap.skilltrack_backend_java.skilltrack.dto.GoalResponseDTO;
import br.com.fiap.skilltrack_backend_java.skilltrack.model.Goal;
import br.com.fiap.skilltrack_backend_java.skilltrack.model.Skill;
import br.com.fiap.skilltrack_backend_java.skilltrack.model.Usuario;

public class GoalMapper {

    public static GoalResponseDTO toResponseDTO(Goal goal) {
        if (goal == null) return null;

        Long skillId = goal.getSkill() != null ? goal.getSkill().getId() : null;
        Long userId = goal.getUser() != null ? goal.getUser().getId() : null;

        return new GoalResponseDTO(
                goal.getId(),
                goal.getTitle(),
                goal.getDescription(),
                goal.getDeadline(),
                goal.isCompleted(),
                skillId,
                userId
        );
    }

    public static Goal toEntity(GoalRequestDTO dto, Skill skill, Usuario usuario) {
        if (dto == null) return null;

        Goal goal = new Goal();
        goal.setTitle(dto.getTituloEfetivo());
        goal.setDescription(dto.getDescricaoEfetiva());
        goal.setDeadline(dto.getDeadline());
        goal.setSkill(skill);
        goal.setUser(usuario);

        return goal;
    }
}