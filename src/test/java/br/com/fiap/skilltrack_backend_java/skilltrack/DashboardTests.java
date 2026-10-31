package br.com.fiap.skilltrack_backend_java.skilltrack;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.time.LocalDate;

import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DashboardTests extends TesteDeIntegracao {

    private String tokenAna;
    private Long idAna;

    @BeforeEach
    void setUp() throws Exception {
        tokenAna = registrar("Ana", "ana@email.com");
        idAna = idDoUsuario("ana@email.com");
    }

    @Test
    void progressoDaSkillEhOUltimoRegistroEAMediaConsideraTodasAsSkills() throws Exception {
        var idJava = criarSkill(tokenAna, idAna, "Java");
        criarSkill(tokenAna, idAna, "Kotlin");

        // o registro de 80% é o mais antigo; no mesmo dia, vale o último cadastrado (70%)
        registrarProgresso(tokenAna, idJava, 80, "2026-01-01");
        registrarProgresso(tokenAna, idJava, 40, "2026-01-02");
        registrarProgresso(tokenAna, idJava, 70, "2026-01-02");

        // dados de outro usuário não entram no dashboard da Ana
        var tokenBruno = registrar("Bruno", "bruno@email.com");
        var idBruno = idDoUsuario("bruno@email.com");
        registrarProgresso(tokenBruno, criarSkill(tokenBruno, idBruno, "Go"), 100, "2026-01-03");

        mvc.perform(get("/dashboard").header("Authorization", bearer(tokenAna)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSkills").value(2))
                .andExpect(jsonPath("$.skillNames", contains("Java", "Kotlin")))
                .andExpect(jsonPath("$.skillProgressValues", contains(70.0, 0.0)))
                .andExpect(jsonPath("$.averageProgress").value(35.0))
                .andExpect(jsonPath("$.progressList[0].percentage").value(70))
                .andExpect(jsonPath("$.progressList.length()").value(3));
    }

    @Test
    void dashboardSemSkillsRetornaZero() throws Exception {
        mvc.perform(get("/dashboard").header("Authorization", bearer(tokenAna)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSkills").value(0))
                .andExpect(jsonPath("$.averageProgress").value(0.0))
                .andExpect(jsonPath("$.skillNames").isEmpty());
    }

    @Test
    void proximasMetasMostraSoAsPendentes() throws Exception {
        var idJava = criarSkill(tokenAna, idAna, "Java");
        for (int i = 0; i < 5; i++) {
            var idConcluida = criarMeta(tokenAna, idAna, idJava, "Concluída " + i);
            mvc.perform(post("/goals/" + idConcluida + "/complete").header("Authorization", bearer(tokenAna)))
                    .andExpect(status().isNoContent());
        }
        criarMeta(tokenAna, idAna, idJava, "Pendente");

        mvc.perform(get("/dashboard").header("Authorization", bearer(tokenAna)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalGoals").value(6))
                .andExpect(jsonPath("$.goals[*].title", contains("Pendente")));
    }

    @Test
    void proximasMetasOrdenaPeloPrazoMaisProximo() throws Exception {
        var idJava = criarSkill(tokenAna, idAna, "Java");
        var hoje = LocalDate.now();
        criarMetaComPrazo(idJava, "Depois", hoje.plusDays(10));
        criarMetaComPrazo(idJava, "Antes", hoje.plusDays(1));

        mvc.perform(get("/dashboard").header("Authorization", bearer(tokenAna)))
                .andExpect(jsonPath("$.goals[*].title", contains("Antes", "Depois")));
    }

    private void registrarProgresso(String token, Long skillId, int porcentagem, String data) throws Exception {
        mvc.perform(post("/progress").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"skill_id\":" + skillId + ",\"percentage\":" + porcentagem + ",\"date\":\"" + data + "\"}"))
                .andExpect(status().isCreated());
    }

    private void criarMetaComPrazo(Long skillId, String titulo, LocalDate prazo) throws Exception {
        mvc.perform(post("/goals").header("Authorization", bearer(tokenAna))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"" + titulo + "\",\"deadline\":\"" + prazo + "\",\"skill_id\":" + skillId
                                + ",\"user_id\":" + idAna + "}"))
                .andExpect(status().isCreated());
    }
}
