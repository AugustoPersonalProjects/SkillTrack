package br.com.fiap.skilltrack_backend_java.skilltrack.mapper;

import br.com.fiap.skilltrack_backend_java.skilltrack.dto.GoalRequestDTO;
import br.com.fiap.skilltrack_backend_java.skilltrack.dto.GoalResponseDTO;
import br.com.fiap.skilltrack_backend_java.skilltrack.model.Goal;
import br.com.fiap.skilltrack_backend_java.skilltrack.model.Skill;

public class GoalMapper {

    public static GoalResponseDTO toResponseDTO(Goal goal) {
        if (goal == null) return null;

        Long skillId = goal.getSkill() != null ? goal.getSkill().getId() : null;

        return new GoalResponseDTO(
                goal.getId(),
                goal.getDescription(),
                goal.getDeadline(),
                skillId
        );
    }

    public static Goal toEntity(GoalRequestDTO dto, Skill skill) {
        if (dto == null) return null;

        Goal goal = new Goal();
        goal.setDescription(dto.getDescription());
        goal.setDeadline(dto.getDeadline());
        goal.setSkill(skill);

        return goal;
    }
}
