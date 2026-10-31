package br.com.fiap.skilltrack_backend_java.skilltrack;

import br.com.fiap.skilltrack_backend_java.skilltrack.config.ClockConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Clock;
import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Hoje é um sábado, 31/10/2026, às 22h em São Paulo, quando em UTC já é 01/11. As datas de criação
// são gravadas direto no banco, porque a aplicação usa a hora real ao criar os registros
class AdminDashboardTests extends TesteDeIntegracao {

    private static final Instant AGORA = Instant.parse("2026-11-01T01:00:00Z");

    @TestConfiguration
    static class RelogioFixo {
        @Bean
        @Primary
        Clock relogioFixo() {
            return Clock.fixed(AGORA, ClockConfig.FUSO_HORARIO);
        }
    }

    private String tokenAdmin;

    @BeforeEach
    void setUp() throws Exception {
        tokenAdmin = criarAdmin();
        definirCriacaoDoUsuario("admin@email.com", "2020-01-01 12:00:00");
    }

    @Test
    void sistemaSemDadosRetornaZeros() throws Exception {
        painel()
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.indicadores.totalUsuarios").value(1))
                .andExpect(jsonPath("$.indicadores.novosUsuarios30Dias").value(0))
                .andExpect(jsonPath("$.indicadores.usuariosAtivos30Dias").value(0))
                .andExpect(jsonPath("$.indicadores.totalSkills").value(0))
                .andExpect(jsonPath("$.indicadores.progressoMedio").value(0))
                .andExpect(jsonPath("$.indicadores.totalGoals").value(0))
                .andExpect(jsonPath("$.indicadores.completedGoals").value(0))
                .andExpect(jsonPath("$.indicadores.overdueGoals").value(0))
                .andExpect(jsonPath("$.novosUsuariosPorMes.length()").value(12))
                .andExpect(jsonPath("$.progressosPorSemana.length()").value(12))
                .andExpect(jsonPath("$.registrosPorMes.length()").value(12))
                .andExpect(jsonPath("$.skillsPorNivel.length()").value(4))
                .andExpect(jsonPath("$.skillsPorCategoria.length()").value(5))
                .andExpect(jsonPath("$.usuariosMaisAtivos.length()").value(0))
                .andExpect(jsonPath("$.skillsSemData").value(0))
                .andExpect(jsonPath("$.metasSemData").value(0));
    }

    @Test
    void novosUsuariosSeguemOCalendarioDoBrasil() throws Exception {
        // 22h de 30/09 em São Paulo, quando em UTC já é outubro
        registrarCriadoEm("Ana", "ana@email.com", "2026-10-01 01:00:00");
        // meia-noite de 01/10 em São Paulo
        registrarCriadoEm("Bruno", "bruno@email.com", "2026-10-01 03:00:00");
        // primeiro instante do primeiro mês do gráfico
        registrarCriadoEm("Carla", "carla@email.com", "2025-11-01 03:00:00");
        // último instante antes do gráfico começar
        registrarCriadoEm("Davi", "davi@email.com", "2025-11-01 02:59:59");

        painel()
                .andExpect(jsonPath("$.novosUsuariosPorMes[0].periodo").value("2025-11"))
                .andExpect(jsonPath("$.novosUsuariosPorMes[0].total").value(1))
                .andExpect(jsonPath("$.novosUsuariosPorMes[5].periodo").value("2026-04"))
                .andExpect(jsonPath("$.novosUsuariosPorMes[5].total").value(0))
                .andExpect(jsonPath("$.novosUsuariosPorMes[10].periodo").value("2026-09"))
                .andExpect(jsonPath("$.novosUsuariosPorMes[10].total").value(1))
                .andExpect(jsonPath("$.novosUsuariosPorMes[11].periodo").value("2026-10"))
                .andExpect(jsonPath("$.novosUsuariosPorMes[11].total").value(1));
    }

    @Test
    void novosUsuariosConsideramOsUltimos30Dias() throws Exception {
        // meia-noite de 02/10 em São Paulo: 30 dias contando com hoje
        registrarCriadoEm("Ana", "ana@email.com", "2026-10-02 03:00:00");
        registrarCriadoEm("Bruno", "bruno@email.com", "2026-10-02 02:59:59");

        painel()
                .andExpect(jsonPath("$.indicadores.totalUsuarios").value(3))
                .andExpect(jsonPath("$.indicadores.novosUsuarios30Dias").value(1));
    }

    @Test
    void usuariosAtivosTemProgressoNosUltimos30Dias() throws Exception {
        var tokenAna = registrarCriadoEm("Ana", "ana@email.com", "2026-01-10 12:00:00");
        var tokenBruno = registrarCriadoEm("Bruno", "bruno@email.com", "2026-01-10 12:00:00");
        var tokenCarla = registrarCriadoEm("Carla", "carla@email.com", "2026-01-10 12:00:00");
        var tokenDavi = registrarCriadoEm("Davi", "davi@email.com", "2026-01-10 12:00:00");
        var skillAna = criarSkill(tokenAna, idDoUsuario("ana@email.com"), "Java");
        var skillBruno = criarSkill(tokenBruno, idDoUsuario("bruno@email.com"), "Go");
        var skillCarla = criarSkill(tokenCarla, idDoUsuario("carla@email.com"), "Rust");
        var skillDavi = criarSkill(tokenDavi, idDoUsuario("davi@email.com"), "C");

        registrarProgresso(tokenAna, skillAna, 10, "2026-10-31");
        registrarProgresso(tokenAna, skillAna, 20, "2026-10-02");
        registrarProgresso(tokenBruno, skillBruno, 10, "2026-10-10");
        registrarProgresso(tokenBruno, skillBruno, 20, "2026-10-11");
        registrarProgresso(tokenBruno, skillBruno, 30, "2026-10-12");
        // fora do período: um dia antes do início e uma data futura
        registrarProgresso(tokenCarla, skillCarla, 10, "2026-10-01");
        registrarProgresso(tokenDavi, skillDavi, 10, "2026-11-01");

        painel()
                .andExpect(jsonPath("$.indicadores.usuariosAtivos30Dias").value(2))
                .andExpect(jsonPath("$.usuariosMaisAtivos.length()").value(2))
                .andExpect(jsonPath("$.usuariosMaisAtivos[0].id_usuario").value(idDoUsuario("bruno@email.com")))
                .andExpect(jsonPath("$.usuariosMaisAtivos[0].nome").value("Bruno"))
                .andExpect(jsonPath("$.usuariosMaisAtivos[0].cor_avatar").value("ESMERALDA"))
                .andExpect(jsonPath("$.usuariosMaisAtivos[0].registros").value(3))
                .andExpect(jsonPath("$.usuariosMaisAtivos[1].nome").value("Ana"))
                .andExpect(jsonPath("$.usuariosMaisAtivos[1].registros").value(2));
    }

    @Test
    void usuariosMaisAtivosSaoNoMaximoCinco() throws Exception {
        for (var nome : new String[]{"Ana", "Bruno", "Carla", "Davi", "Eva", "Fabi"}) {
            var email = nome.toLowerCase() + "@email.com";
            var token = registrarCriadoEm(nome, email, "2026-01-10 12:00:00");
            registrarProgresso(token, criarSkill(token, idDoUsuario(email), "Java"), 10, "2026-10-20");
        }

        painel()
                .andExpect(jsonPath("$.indicadores.usuariosAtivos30Dias").value(6))
                .andExpect(jsonPath("$.usuariosMaisAtivos.length()").value(5))
                // no empate, a ordem é alfabética
                .andExpect(jsonPath("$.usuariosMaisAtivos[0].nome").value("Ana"))
                .andExpect(jsonPath("$.usuariosMaisAtivos[4].nome").value("Eva"));
    }

    @Test
    void progressoMedioUsaOUltimoRegistroDeCadaSkillEContaSkillSemRegistroComoZero() throws Exception {
        var token = registrarCriadoEm("Ana", "ana@email.com", "2026-01-10 12:00:00");
        var idAna = idDoUsuario("ana@email.com");
        var maiorData = criarSkill(token, idAna, "Java");
        var mesmaData = criarSkill(token, idAna, "Go");
        criarSkill(token, idAna, "Sem registro");

        registrarProgresso(token, maiorData, 80, "2026-10-20");
        registrarProgresso(token, maiorData, 10, "2026-10-01");
        registrarProgresso(token, mesmaData, 90, "2026-10-15");
        registrarProgresso(token, mesmaData, 40, "2026-10-15");

        // (80 + 40 + 0) / 3
        painel()
                .andExpect(jsonPath("$.indicadores.totalSkills").value(3))
                .andExpect(jsonPath("$.indicadores.progressoMedio").value(40));
    }

    @Test
    void metasSaoSeparadasPorSituacaoNoCalendarioDoBrasil() throws Exception {
        var token = registrarCriadoEm("Ana", "ana@email.com", "2026-01-10 12:00:00");
        var idAna = idDoUsuario("ana@email.com");
        var idSkill = criarSkill(token, idAna, "Java");

        var concluidaComPrazoVencido = criarMeta(token, idAna, idSkill, "Concluída");
        var atrasada = criarMeta(token, idAna, idSkill, "Atrasada");
        var venceHoje = criarMeta(token, idAna, idSkill, "Vence hoje");
        criarMeta(token, idAna, idSkill, "Sem prazo");

        concluir(token, concluidaComPrazoVencido);
        definirPrazo(concluidaComPrazoVencido, "2026-01-01");
        definirPrazo(atrasada, "2026-10-30");
        // em UTC já é 01/11, mas no Brasil o prazo ainda não passou
        definirPrazo(venceHoje, "2026-10-31");

        painel()
                .andExpect(jsonPath("$.indicadores.totalGoals").value(4))
                .andExpect(jsonPath("$.indicadores.completedGoals").value(1))
                .andExpect(jsonPath("$.indicadores.overdueGoals").value(1));
    }

    @Test
    void registrosPorMesContamSoOQueTemData() throws Exception {
        var token = registrarCriadoEm("Ana", "ana@email.com", "2026-01-10 12:00:00");
        var idAna = idDoUsuario("ana@email.com");
        var skillComData = criarSkill(token, idAna, "Java");
        var skillAntiga = criarSkill(token, idAna, "Go");
        var metaComData = criarMeta(token, idAna, skillComData, "Com data");
        var metaAntiga = criarMeta(token, idAna, skillComData, "Antiga");

        jdbcTemplate.update("UPDATE st_skills SET data_criacao = ? WHERE id_skill = ?", "2026-10-15 12:00:00", skillComData);
        jdbcTemplate.update("UPDATE st_skills SET data_criacao = NULL WHERE id_skill = ?", skillAntiga);
        jdbcTemplate.update("UPDATE st_goals SET data_criacao = ? WHERE id_goal = ?", "2026-09-10 12:00:00", metaComData);
        jdbcTemplate.update("UPDATE st_goals SET data_criacao = NULL WHERE id_goal = ?", metaAntiga);
        // o relógio está em 01/11 em UTC, mas a conclusão conta para outubro no Brasil
        concluir(token, metaComData);
        // meta antiga concluída antes de a data ser registrada
        jdbcTemplate.update("UPDATE st_goals SET completed = TRUE WHERE id_goal = ?", metaAntiga);

        painel()
                .andExpect(jsonPath("$.registrosPorMes[10].periodo").value("2026-09"))
                .andExpect(jsonPath("$.registrosPorMes[10].skills").value(0))
                .andExpect(jsonPath("$.registrosPorMes[10].metas").value(1))
                .andExpect(jsonPath("$.registrosPorMes[10].metasConcluidas").value(0))
                .andExpect(jsonPath("$.registrosPorMes[11].periodo").value("2026-10"))
                .andExpect(jsonPath("$.registrosPorMes[11].skills").value(1))
                .andExpect(jsonPath("$.registrosPorMes[11].metas").value(0))
                .andExpect(jsonPath("$.registrosPorMes[11].metasConcluidas").value(1))
                .andExpect(jsonPath("$.skillsSemData").value(1))
                .andExpect(jsonPath("$.metasSemData").value(1))
                // os registros sem data continuam nos totais
                .andExpect(jsonPath("$.indicadores.totalSkills").value(2))
                .andExpect(jsonPath("$.indicadores.totalGoals").value(2))
                .andExpect(jsonPath("$.indicadores.completedGoals").value(2));
    }

    @Test
    void progressosSaoAgrupadosPorSemanaComecandoNaSegunda() throws Exception {
        var token = registrarCriadoEm("Ana", "ana@email.com", "2026-01-10 12:00:00");
        var idSkill = criarSkill(token, idDoUsuario("ana@email.com"), "Java");

        registrarProgresso(token, idSkill, 10, "2026-10-26");
        registrarProgresso(token, idSkill, 20, "2026-10-31");
        registrarProgresso(token, idSkill, 30, "2026-10-25");
        registrarProgresso(token, idSkill, 40, "2026-08-10");
        // fora do gráfico: o domingo anterior à primeira semana e uma data futura
        registrarProgresso(token, idSkill, 50, "2026-08-09");
        registrarProgresso(token, idSkill, 60, "2026-11-01");

        painel()
                .andExpect(jsonPath("$.progressosPorSemana[0].periodo").value("2026-08-10"))
                .andExpect(jsonPath("$.progressosPorSemana[0].total").value(1))
                .andExpect(jsonPath("$.progressosPorSemana[1].total").value(0))
                .andExpect(jsonPath("$.progressosPorSemana[10].periodo").value("2026-10-19"))
                .andExpect(jsonPath("$.progressosPorSemana[10].total").value(1))
                .andExpect(jsonPath("$.progressosPorSemana[11].periodo").value("2026-10-26"))
                .andExpect(jsonPath("$.progressosPorSemana[11].total").value(2));
    }

    @Test
    void skillsSaoContadasPorCategoriaEPorNivel() throws Exception {
        var token = registrarCriadoEm("Ana", "ana@email.com", "2026-01-10 12:00:00");
        var idAna = idDoUsuario("ana@email.com");
        var categorias = categoryRepository.findAll();
        var maisUsada = categorias.get(3);
        var menosUsada = categorias.get(1);

        criarSkillCom(token, idAna, "A", maisUsada.getId(), "EXPERT");
        criarSkillCom(token, idAna, "B", maisUsada.getId(), "BEGINNER");
        criarSkillCom(token, idAna, "C", menosUsada.getId(), "BEGINNER");

        painel()
                .andExpect(jsonPath("$.skillsPorCategoria.length()").value(5))
                .andExpect(jsonPath("$.skillsPorCategoria[0].categoria").value(maisUsada.getName()))
                .andExpect(jsonPath("$.skillsPorCategoria[0].total").value(2))
                .andExpect(jsonPath("$.skillsPorCategoria[1].categoria").value(menosUsada.getName()))
                .andExpect(jsonPath("$.skillsPorCategoria[1].total").value(1))
                .andExpect(jsonPath("$.skillsPorCategoria[4].total").value(0))
                .andExpect(jsonPath("$.skillsPorNivel[0].nivel").value("BEGINNER"))
                .andExpect(jsonPath("$.skillsPorNivel[0].total").value(2))
                .andExpect(jsonPath("$.skillsPorNivel[1].nivel").value("INTERMEDIATE"))
                .andExpect(jsonPath("$.skillsPorNivel[1].total").value(0))
                .andExpect(jsonPath("$.skillsPorNivel[2].nivel").value("ADVANCED"))
                .andExpect(jsonPath("$.skillsPorNivel[3].nivel").value("EXPERT"))
                .andExpect(jsonPath("$.skillsPorNivel[3].total").value(1));
    }

    @Test
    void somenteAdminAcessaOPainel() throws Exception {
        var tokenAna = registrar("Ana", "ana@email.com");

        mvc.perform(get("/admin/dashboard").header("Authorization", bearer(tokenAna)))
                .andExpect(status().isForbidden());
        mvc.perform(get("/admin/dashboard"))
                .andExpect(status().isUnauthorized());
    }

    private ResultActions painel() throws Exception {
        return mvc.perform(get("/admin/dashboard").header("Authorization", bearer(tokenAdmin)));
    }

    private String registrarCriadoEm(String nome, String email, String dataUtc) throws Exception {
        var token = registrar(nome, email);
        definirCriacaoDoUsuario(email, dataUtc);
        return token;
    }

    private void definirCriacaoDoUsuario(String email, String dataUtc) {
        jdbcTemplate.update("UPDATE st_usuarios SET data_criacao = ? WHERE email = ?", dataUtc, email);
    }

    private void definirPrazo(Long idMeta, String prazo) {
        jdbcTemplate.update("UPDATE st_goals SET deadline = ? WHERE id_goal = ?", prazo, idMeta);
    }

    private void concluir(String token, Long idMeta) throws Exception {
        mvc.perform(post("/goals/" + idMeta + "/complete").header("Authorization", bearer(token)))
                .andExpect(status().is2xxSuccessful());
    }

    private void registrarProgresso(String token, Long idSkill, int porcentagem, String data) throws Exception {
        mvc.perform(post("/progress").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"skill_id\":" + idSkill + ",\"percentage\":" + porcentagem + ",\"date\":\"" + data + "\"}"))
                .andExpect(status().isCreated());
    }

    private void criarSkillCom(String token, Long idUsuario, String nome, Long idCategoria, String nivel) throws Exception {
        mvc.perform(post("/api/skills").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + nome + "\",\"categoryId\":" + idCategoria
                                + ",\"level\":\"" + nivel + "\",\"userId\":" + idUsuario + "}"))
                .andExpect(status().isCreated());
    }
}
