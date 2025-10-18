package br.com.fiap.skilltrack_backend_java.skilltrack.mapper;

import br.com.fiap.skilltrack_backend_java.skilltrack.dto.ProgressRequestDTO;
import br.com.fiap.skilltrack_backend_java.skilltrack.dto.ProgressResponseDTO;
import br.com.fiap.skilltrack_backend_java.skilltrack.model.Progress;
import br.com.fiap.skilltrack_backend_java.skilltrack.model.Skill;


public class ProgressMapper {

    public static ProgressResponseDTO toResponseDTO(Progress progress) {
        if (progress == null) return null;

        Long skillId = progress.getSkill() != null ? progress.getSkill().getId() : null;

        return new ProgressResponseDTO(
                progress.getId(),
                progress.getPercentage(),
                progress.getDate(),
                skillId
        );
    }

    public static Progress toEntity(ProgressRequestDTO dto, Skill skill) {
        if (dto == null) return null;

        Progress progress = new Progress();
        progress.setSkill(skill);
        progress.setPercentage(dto.getPercentage());
        progress.setDate(dto.getDate());

        return progress;
    }
}
