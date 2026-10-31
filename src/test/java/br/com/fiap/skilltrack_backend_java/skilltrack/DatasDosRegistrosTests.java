package br.com.fiap.skilltrack_backend_java.skilltrack;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DatasDosRegistrosTests extends TesteDeIntegracao {

    private String tokenAna;
    private Long idAna;
    private Long idSkill;

    @BeforeEach
    void setUp() throws Exception {
        tokenAna = registrar("Ana", "ana@email.com");
        idAna = idDoUsuario("ana@email.com");
        idSkill = criarSkill(tokenAna, idAna, "Java");
    }

    @Test
    void dataDeCriacaoDoUsuarioEhUmInstanteEmUtc() throws Exception {
        var resposta = mvc.perform(get("/usuarios/me").header("Authorization", bearer(tokenAna)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String dataCriacao = JsonPath.read(resposta, "$.data_criacao");

        // com o "Z", o navegador converte para o fuso de quem está vendo
        assertThat(dataCriacao).endsWith("Z");
        assertThat(Instant.parse(dataCriacao)).isCloseTo(Instant.now(), within(1, ChronoUnit.MINUTES));
    }

    @Test
    void skillEMetaGuardamADataDeCriacao() throws Exception {
        var idMeta = criarMeta(tokenAna, idAna, idSkill, "Meta");

        assertThat(dataDaSkill(idSkill))
                .isCloseTo(Instant.now(), within(1, ChronoUnit.MINUTES));
        assertThat(dataDaMeta("data_criacao", idMeta))
                .isCloseTo(Instant.now(), within(1, ChronoUnit.MINUTES));
        assertThat(dataDaMeta("data_conclusao", idMeta)).isNull();
    }

    @Test
    void concluirAMetaGuardaADataDaPrimeiraConclusao() throws Exception {
        var idMeta = criarMeta(tokenAna, idAna, idSkill, "Meta");

        mvc.perform(post("/goals/" + idMeta + "/complete").header("Authorization", bearer(tokenAna)))
                .andExpect(status().is2xxSuccessful());
        assertThat(dataDaMeta("data_conclusao", idMeta))
                .isCloseTo(Instant.now(), within(1, ChronoUnit.MINUTES));

        var primeiraConclusao = "2026-01-15 12:00:00";
        jdbcTemplate.update("UPDATE st_goals SET data_conclusao = ? WHERE id_goal = ?", primeiraConclusao, idMeta);
        mvc.perform(post("/goals/" + idMeta + "/complete").header("Authorization", bearer(tokenAna)))
                .andExpect(status().is2xxSuccessful());

        assertThat(dataDaMeta("data_conclusao", idMeta))
                .isEqualTo(Instant.parse("2026-01-15T12:00:00Z"));
    }

    private Instant dataDaSkill(Long id) {
        return lerData("SELECT DATE_FORMAT(data_criacao, ?) FROM st_skills WHERE id_skill = ?", id);
    }

    private Instant dataDaMeta(String coluna, Long id) {
        return lerData("SELECT DATE_FORMAT(" + coluna + ", ?) FROM st_goals WHERE id_goal = ?", id);
    }

    // lê a coluna como texto, para conferir o que está gravado sem a conversão de fuso do driver
    private Instant lerData(String sql, Long id) {
        var data = jdbcTemplate.queryForObject(sql, String.class, "%Y-%m-%dT%H:%i:%sZ", id);
        return data != null ? Instant.parse(data) : null;
    }
}
