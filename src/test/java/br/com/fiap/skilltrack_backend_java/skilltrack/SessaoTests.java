package br.com.fiap.skilltrack_backend_java.skilltrack;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SessaoTests extends TesteDeIntegracao {

    @Value("${api.security.token.secret}")
    private String segredo;

    private String tokenAna;
    private String tokenAdmin;
    private Long idAna;

    @BeforeEach
    void setUp() throws Exception {
        tokenAna = registrar("Ana", "ana@email.com");
        tokenAdmin = criarAdmin();
        idAna = idDoUsuario("ana@email.com");
    }

    @Test
    void tokenIdentificaOUsuarioPeloId() {
        var token = JWT.decode(tokenAna);

        assertThat(token.getSubject()).isEqualTo(idAna.toString());
        // o front já publicado lê o id por esta claim
        assertThat(token.getClaim("id").asLong()).isEqualTo(idAna);
    }

    @Test
    void trocarOEmailNaoEncerraASessao() throws Exception {
        mvc.perform(put("/usuarios/" + idAna).header("Authorization", bearer(tokenAdmin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Ana\",\"email\":\"novo@email.com\"}"))
                .andExpect(status().isOk());

        mvc.perform(get("/usuarios/" + idAna).header("Authorization", bearer(tokenAna)))
                .andExpect(status().isOk());
    }

    @Test
    void trocarASenhaEncerraAsSessoesAbertas() throws Exception {
        mvc.perform(put("/usuarios/" + idAna).header("Authorization", bearer(tokenAdmin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Ana\",\"email\":\"ana@email.com\",\"senha\":\"novaSenha1\"}"))
                .andExpect(status().isOk());

        mvc.perform(get("/usuarios/" + idAna).header("Authorization", bearer(tokenAna)))
                .andExpect(status().isUnauthorized());

        var tokenNovo = login("ana@email.com", "novaSenha1");
        mvc.perform(get("/usuarios/" + idAna).header("Authorization", bearer(tokenNovo)))
                .andExpect(status().isOk());
    }

    @Test
    void alterarOutrosDadosNaoEncerraASessao() throws Exception {
        mvc.perform(put("/usuarios/" + idAna).header("Authorization", bearer(tokenAdmin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Ana Souza\",\"email\":\"ana@email.com\",\"senha\":\"\"}"))
                .andExpect(status().isOk());

        mvc.perform(get("/usuarios/" + idAna).header("Authorization", bearer(tokenAna)))
                .andExpect(status().isOk());
    }

    @Test
    void tokenDeContaExcluidaNaoAbreContaNovaComOMesmoEmail() throws Exception {
        mvc.perform(delete("/usuarios/" + idAna).header("Authorization", bearer(tokenAdmin)))
                .andExpect(status().isNoContent());
        registrar("Outra Ana", "ana@email.com");

        mvc.perform(get("/goals").header("Authorization", bearer(tokenAna)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenNoFormatoAntigoEhRecusado() throws Exception {
        var tokenAntigo = JWT.create()
                .withIssuer("API SkillTrack")
                .withSubject("ana@email.com")
                .withClaim("id", idAna)
                .withExpiresAt(Instant.now().plus(1, ChronoUnit.DAYS))
                .sign(Algorithm.HMAC256(segredo));

        mvc.perform(get("/goals").header("Authorization", bearer(tokenAntigo)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenSemAVersaoEhRecusado() throws Exception {
        var tokenSemVersao = JWT.create()
                .withIssuer("API SkillTrack")
                .withSubject(idAna.toString())
                .withExpiresAt(Instant.now().plus(1, ChronoUnit.DAYS))
                .sign(Algorithm.HMAC256(segredo));

        mvc.perform(get("/goals").header("Authorization", bearer(tokenSemVersao)))
                .andExpect(status().isUnauthorized());
    }
}
