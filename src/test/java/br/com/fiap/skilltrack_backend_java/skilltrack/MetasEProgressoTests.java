package br.com.fiap.skilltrack_backend_java.skilltrack;

import br.com.fiap.skilltrack_backend_java.skilltrack.config.ClockConfig;
import br.com.fiap.skilltrack_backend_java.skilltrack.repository.GoalRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MetasEProgressoTests extends TesteDeIntegracao {

    // 22h de 31/10 em São Paulo, quando em UTC já é 01/11
    private static final Instant AGORA = Instant.parse("2026-11-01T01:00:00Z");

    @TestConfiguration
    static class RelogioFixo {
        @Bean
        @Primary
        Clock relogioFixo() {
            return Clock.fixed(AGORA, ClockConfig.FUSO_HORARIO);
        }
    }

    @Autowired
    private GoalRepository goalRepository;

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
    void prazoDeHojeEhAceitoMesmoQuandoJaEAmanhaEmUtc() throws Exception {
        mvc.perform(post("/goals").header("Authorization", bearer(tokenAna))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(metaComPrazo("Meta", "2026-10-31")))
                .andExpect(status().isCreated());
    }

    @Test
    void prazoPassadoEhRecusadoNaCriacao() throws Exception {
        mvc.perform(post("/goals").header("Authorization", bearer(tokenAna))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(metaComPrazo("Meta", "2026-10-30")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("A data limite deve ser hoje ou uma data futura."));
    }

    @Test
    void metaAtrasadaPodeSerEditadaSemMudarOPrazo() throws Exception {
        var idMeta = criarMetaAtrasada("2026-10-01");

        mvc.perform(put("/goals/" + idMeta).header("Authorization", bearer(tokenAna))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(metaComPrazo("Novo título", "2026-10-01")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Novo título"))
                .andExpect(jsonPath("$.deadline").value("2026-10-01"));
    }

    @Test
    void metaAtrasadaNaoAceitaOutroPrazoPassado() throws Exception {
        var idMeta = criarMetaAtrasada("2026-10-01");

        mvc.perform(put("/goals/" + idMeta).header("Authorization", bearer(tokenAna))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(metaComPrazo("Meta", "2026-10-15")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void progressoSemDataUsaODiaAtualNoBrasil() throws Exception {
        mvc.perform(post("/progress").header("Authorization", bearer(tokenAna))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"skill_id\":" + idSkill + ",\"percentage\":10}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.date").value("2026-10-31"));
    }

    @Test
    void progressoSemPorcentagemEhRecusado() throws Exception {
        mvc.perform(post("/progress").header("Authorization", bearer(tokenAna))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"skill_id\":" + idSkill + "}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].mensagem").value("A porcentagem é obrigatória"));
    }

    @Test
    void adminNaoVinculaMetaASkillDeOutroUsuario() throws Exception {
        var tokenBruno = registrar("Bruno", "bruno@email.com");
        var idBruno = idDoUsuario("bruno@email.com");
        var idSkillBruno = criarSkill(tokenBruno, idBruno, "Go");
        var idMetaAna = criarMetaAtrasada("2026-10-31");
        var tokenAdmin = criarAdmin();

        mvc.perform(post("/goals").header("Authorization", bearer(tokenAdmin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(metaJson(idAna, idSkillBruno, "Meta")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("A skill informada não pertence a este usuário."));
        mvc.perform(put("/goals/" + idMetaAna).header("Authorization", bearer(tokenAdmin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(metaJson(idAna, idSkillBruno, "Meta")))
                .andExpect(status().isBadRequest());
    }

    // o prazo passado é gravado direto no banco porque a API não permite criá-lo
    private Long criarMetaAtrasada(String prazo) throws Exception {
        var resposta = mvc.perform(post("/goals").header("Authorization", bearer(tokenAna))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(metaComPrazo("Meta", "2026-10-31")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Number idMeta = JsonPath.read(resposta, "$.id_goal");

        var meta = goalRepository.findById(idMeta.longValue()).orElseThrow();
        meta.setDeadline(LocalDate.parse(prazo));
        goalRepository.save(meta);
        return meta.getId();
    }

    private String metaComPrazo(String titulo, String prazo) {
        return "{\"title\":\"" + titulo + "\",\"deadline\":\"" + prazo + "\",\"skill_id\":" + idSkill
                + ",\"user_id\":" + idAna + "}";
    }
}
