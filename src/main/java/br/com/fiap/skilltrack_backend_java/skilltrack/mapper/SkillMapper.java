package br.com.fiap.skilltrack_backend_java.skilltrack.mapper;

import br.com.fiap.skilltrack_backend_java.skilltrack.dto.GoalResponseDTO;
import br.com.fiap.skilltrack_backend_java.skilltrack.dto.ProgressResponseDTO;
import br.com.fiap.skilltrack_backend_java.skilltrack.dto.SkillRequestDTO;
import br.com.fiap.skilltrack_backend_java.skilltrack.dto.SkillResponseDTO;
import br.com.fiap.skilltrack_backend_java.skilltrack.model.Skill;

import java.util.List;

public class SkillMapper {

    public static SkillResponseDTO toResponseDTO(Skill skill) {
        if (skill == null) return null;

        List<ProgressResponseDTO> progressHistoryDTO = skill.getProgressHistory() != null ?
                skill.getProgressHistory().stream()
                        .map(ProgressMapper::toResponseDTO)
                        .toList()
                : null;

        List<GoalResponseDTO> goalsDTO = skill.getGoals() != null ?
                skill.getGoals().stream()
                        .map(GoalMapper::toResponseDTO)
                        .toList()
                : null;

        return new SkillResponseDTO(
                skill.getId(),
                skill.getName(),
                skill.getCategory(),
                skill.getLevel(),
                progressHistoryDTO,
                goalsDTO
        );
    }

    public static Skill toEntity(SkillRequestDTO dto) {
        if (dto == null) return null;

        Skill skill = new Skill();
        skill.setName(dto.getName());
        skill.setCategory(dto.getCategory());
        skill.setLevel(dto.getLevel());

        return skill;
    }
}
