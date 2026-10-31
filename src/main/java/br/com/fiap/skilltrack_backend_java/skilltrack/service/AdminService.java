package br.com.fiap.skilltrack_backend_java.skilltrack.service;

import br.com.fiap.skilltrack_backend_java.skilltrack.dto.AdminDashboardDTO;
import br.com.fiap.skilltrack_backend_java.skilltrack.dto.AdminResumoDTO;
import br.com.fiap.skilltrack_backend_java.skilltrack.model.enums.Perfil;
import br.com.fiap.skilltrack_backend_java.skilltrack.model.enums.SkillLevel;
import br.com.fiap.skilltrack_backend_java.skilltrack.repository.CategoryRepository;
import br.com.fiap.skilltrack_backend_java.skilltrack.repository.GoalRepository;
import br.com.fiap.skilltrack_backend_java.skilltrack.repository.ProgressRepository;
import br.com.fiap.skilltrack_backend_java.skilltrack.repository.SkillRepository;
import br.com.fiap.skilltrack_backend_java.skilltrack.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class AdminService {

    private static final int DIAS_DOS_INDICADORES = 30;
    private static final int MESES_DOS_GRAFICOS = 12;
    private static final int SEMANAS_DOS_GRAFICOS = 12;
    private static final int LIMITE_DE_USUARIOS_ATIVOS = 5;

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

    @Autowired
    private Clock clock;

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

    // Os períodos seguem o calendário do Brasil, o mesmo das regras de data do sistema (ClockConfig)
    @Transactional(readOnly = true)
    public AdminDashboardDTO buscarDashboard() {
        var hoje = LocalDate.now(clock);
        var meses = ultimosMeses(hoje);
        var inicioDosMeses = inicioDoDia(meses.getFirst().atDay(1));

        var usuariosPorMes = contarPorMes(usuarioRepository.findDatasDeCriacaoDesde(inicioDosMeses));
        var skillsPorMes = contarPorMes(skillRepository.findDatasDeCriacaoDesde(inicioDosMeses));
        var metasPorMes = contarPorMes(goalRepository.findDatasDeCriacaoDesde(inicioDosMeses));
        var conclusoesPorMes = contarPorMes(goalRepository.findDatasDeConclusaoDesde(inicioDosMeses));

        return new AdminDashboardDTO(
                buscarIndicadores(hoje),
                meses.stream()
                        .map(mes -> new AdminDashboardDTO.TotalDoMes(mes, usuariosPorMes.getOrDefault(mes, 0L)))
                        .toList(),
                contarProgressosPorSemana(hoje),
                meses.stream()
                        .map(mes -> new AdminDashboardDTO.RegistrosDoMes(
                                mes,
                                skillsPorMes.getOrDefault(mes, 0L),
                                metasPorMes.getOrDefault(mes, 0L),
                                conclusoesPorMes.getOrDefault(mes, 0L)))
                        .toList(),
                skillRepository.countByDataCriacaoIsNull(),
                goalRepository.countByDataCriacaoIsNull(),
                skillRepository.countGroupedByCategory().stream()
                        .map(linha -> new AdminDashboardDTO.TotalDaCategoria(linha.getCategoria(), linha.getTotal()))
                        .toList(),
                contarSkillsPorNivel(),
                buscarUsuariosMaisAtivos(hoje)
        );
    }

    private AdminDashboardDTO.Indicadores buscarIndicadores(LocalDate hoje) {
        var inicioDoPeriodo = inicioDosIndicadores(hoje);
        var totalSkills = skillRepository.count();
        // uma skill sem registros tem progresso 0 e também entra na média
        var progressoMedio = totalSkills == 0 ? 0 : Math.round((float) progressRepository.sumLatestPercentages() / totalSkills);

        return new AdminDashboardDTO.Indicadores(
                usuarioRepository.count(),
                usuarioRepository.countByDataCriacaoGreaterThanEqual(inicioDoDia(inicioDoPeriodo)),
                progressRepository.countUsuariosComProgressoEntre(inicioDoPeriodo, hoje),
                totalSkills,
                progressoMedio,
                goalRepository.count(),
                goalRepository.countByCompletedTrue(),
                goalRepository.countByCompletedFalseAndDeadlineBefore(hoje)
        );
    }

    private List<AdminDashboardDTO.TotalDaSemana> contarProgressosPorSemana(LocalDate hoje) {
        var primeiraSemana = segundaDaSemana(hoje).minusWeeks(SEMANAS_DOS_GRAFICOS - 1);
        var porSemana = progressRepository.findDatasEntre(primeiraSemana, hoje).stream()
                .collect(Collectors.groupingBy(this::segundaDaSemana, Collectors.counting()));

        return Stream.iterate(primeiraSemana, semana -> semana.plusWeeks(1))
                .limit(SEMANAS_DOS_GRAFICOS)
                .map(semana -> new AdminDashboardDTO.TotalDaSemana(semana, porSemana.getOrDefault(semana, 0L)))
                .toList();
    }

    private List<AdminDashboardDTO.TotalDoNivel> contarSkillsPorNivel() {
        var porNivel = skillRepository.countGroupedByLevel().stream()
                .collect(Collectors.toMap(SkillRepository.TotalPorNivel::getNivel, SkillRepository.TotalPorNivel::getTotal));

        return Arrays.stream(SkillLevel.values())
                .map(nivel -> new AdminDashboardDTO.TotalDoNivel(nivel, porNivel.getOrDefault(nivel, 0L)))
                .toList();
    }

    private List<AdminDashboardDTO.UsuarioAtivo> buscarUsuariosMaisAtivos(LocalDate hoje) {
        return progressRepository.findUsuariosMaisAtivos(inicioDosIndicadores(hoje), hoje, Limit.of(LIMITE_DE_USUARIOS_ATIVOS)).stream()
                .map(linha -> new AdminDashboardDTO.UsuarioAtivo(
                        linha.getIdUsuario(), linha.getNome(), linha.getCorAvatar(), linha.getRegistros()))
                .toList();
    }

    private LocalDate inicioDosIndicadores(LocalDate hoje) {
        return hoje.minusDays(DIAS_DOS_INDICADORES - 1);
    }

    private List<YearMonth> ultimosMeses(LocalDate hoje) {
        return Stream.iterate(YearMonth.from(hoje).minusMonths(MESES_DOS_GRAFICOS - 1), mes -> mes.plusMonths(1))
                .limit(MESES_DOS_GRAFICOS)
                .toList();
    }

    private Map<YearMonth, Long> contarPorMes(List<Instant> instantes) {
        return instantes.stream()
                .collect(Collectors.groupingBy(instante -> YearMonth.from(instante.atZone(clock.getZone())), Collectors.counting()));
    }

    private LocalDate segundaDaSemana(LocalDate data) {
        return data.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    private Instant inicioDoDia(LocalDate data) {
        return data.atStartOfDay(clock.getZone()).toInstant();
    }
}
