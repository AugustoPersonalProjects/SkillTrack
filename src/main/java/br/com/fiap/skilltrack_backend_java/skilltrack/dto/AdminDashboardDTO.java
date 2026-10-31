package br.com.fiap.skilltrack_backend_java.skilltrack.dto;

import br.com.fiap.skilltrack_backend_java.skilltrack.model.enums.CorAvatar;
import br.com.fiap.skilltrack_backend_java.skilltrack.model.enums.SkillLevel;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

public record AdminDashboardDTO(
        Indicadores indicadores,
        List<TotalDoMes> novosUsuariosPorMes,
        List<TotalDaSemana> progressosPorSemana,
        List<RegistrosDoMes> registrosPorMes,
        long skillsSemData,
        long metasSemData,
        List<TotalDaCategoria> skillsPorCategoria,
        List<TotalDoNivel> skillsPorNivel,
        List<UsuarioAtivo> usuariosMaisAtivos
) {
    public record Indicadores(
            long totalUsuarios,
            long novosUsuarios30Dias,
            long usuariosAtivos30Dias,
            long totalSkills,
            int progressoMedio,
            long totalGoals,
            long completedGoals,
            long overdueGoals
    ) {}

    public record TotalDoMes(YearMonth periodo, long total) {}

    // o período é a segunda-feira da semana
    public record TotalDaSemana(LocalDate periodo, long total) {}

    public record RegistrosDoMes(YearMonth periodo, long skills, long metas, long metasConcluidas) {}

    public record TotalDaCategoria(String categoria, long total) {}

    public record TotalDoNivel(SkillLevel nivel, long total) {}

    public record UsuarioAtivo(
            @JsonProperty("id_usuario") Long id,
            String nome,
            @JsonProperty("cor_avatar") CorAvatar corAvatar,
            long registros
    ) {}
}
