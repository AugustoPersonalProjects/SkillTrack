package br.com.fiap.skilltrack_backend_java.skilltrack;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AutorizacaoDonoDadosTests extends TesteDeIntegracao {

    private String tokenAna;
    private String tokenBruno;
    private Long idAna;
    private Long idBruno;

    @BeforeEach
    void setUp() throws Exception {
        tokenAna = registrar("Ana", "ana@email.com");
        tokenBruno = registrar("Bruno", "bruno@email.com");
        idAna = idDoUsuario("ana@email.com");
        idBruno = idDoUsuario("bruno@email.com");
    }

    @Test
    void usuarioNaoAcessaSkillsDeOutroUsuario() throws Exception {
        var idSkill = criarSkill(tokenAna, idAna, "Java");

        mvc.perform(get("/api/skills/" + idSkill).header("Authorization", bearer(tokenBruno)))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/skills/user/" + idAna).header("Authorization", bearer(tokenBruno)))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/skills/" + idSkill).header("Authorization", bearer(tokenBruno)))
                .andExpect(status().isForbidden());

        // a listagem geral mostra só as skills do próprio usuário
        mvc.perform(get("/api/skills").header("Authorization", bearer(tokenBruno)))
                .andExpect(jsonPath("$.page.totalElements").value(0));
        mvc.perform(get("/api/skills/" + idSkill).header("Authorization", bearer(tokenAna)))
                .andExpect(status().isOk());
    }

    @Test
    void usuarioNaoAlteraSkillDeOutroUsuario() throws Exception {
        var idSkill = criarSkill(tokenAna, idAna, "Java");

        mvc.perform(put("/api/skills/" + idSkill).header("Authorization", bearer(tokenBruno))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(skillJson(idBruno, "Alterada")))
                .andExpect(status().isForbidden());

        mvc.perform(get("/api/skills/" + idSkill).header("Authorization", bearer(tokenAna)))
                .andExpect(jsonPath("$.name").value("Java"));
    }

    @Test
    void usuarioNaoCriaRegistrosEmNomeDeOutroUsuario() throws Exception {
        var idSkillAna = criarSkill(tokenAna, idAna, "Java");

        mvc.perform(post("/api/skills").header("Authorization", bearer(tokenBruno))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(skillJson(idAna, "Java")))
                .andExpect(status().isForbidden());

        // meta do Bruno vinculada à skill da Ana
        mvc.perform(post("/goals").header("Authorization", bearer(tokenBruno))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(metaJson(idBruno, idSkillAna, "Meta")))
                .andExpect(status().isForbidden());

        mvc.perform(post("/progress").header("Authorization", bearer(tokenBruno))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"skill_id\":" + idSkillAna + ",\"percentage\":50}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void usuarioNaoAcessaMetasDeOutroUsuario() throws Exception {
        var idSkill = criarSkill(tokenAna, idAna, "Java");
        var idGoal = criarMeta(tokenAna, idAna, idSkill, "Meta");

        mvc.perform(get("/goals/" + idGoal).header("Authorization", bearer(tokenBruno)))
                .andExpect(status().isForbidden());
        mvc.perform(get("/goals/user/" + idAna).header("Authorization", bearer(tokenBruno)))
                .andExpect(status().isForbidden());
        mvc.perform(post("/goals/" + idGoal + "/complete").header("Authorization", bearer(tokenBruno)))
                .andExpect(status().isForbidden());
        mvc.perform(get("/goals").header("Authorization", bearer(tokenBruno)))
                .andExpect(jsonPath("$.totalElements").value(0));
        mvc.perform(get("/goals/user/" + idAna).header("Authorization", bearer(tokenAna)))
                .andExpect(status().isOk());
    }

    @Test
    void usuarioNaoAlteraNemExcluiMetaDeOutroUsuario() throws Exception {
        var idSkillAna = criarSkill(tokenAna, idAna, "Java");
        var idSkillBruno = criarSkill(tokenBruno, idBruno, "Go");
        var idGoal = criarMeta(tokenAna, idAna, idSkillAna, "Meta");

        mvc.perform(put("/goals/" + idGoal).header("Authorization", bearer(tokenBruno))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(metaJson(idBruno, idSkillBruno, "Alterada")))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/goals/" + idGoal).header("Authorization", bearer(tokenBruno)))
                .andExpect(status().isForbidden());

        mvc.perform(get("/goals/" + idGoal).header("Authorization", bearer(tokenAna)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Meta"));
    }

    @Test
    void usuarioNaoMoveMetaParaSkillDeOutroUsuario() throws Exception {
        var idSkillAna = criarSkill(tokenAna, idAna, "Java");
        var idSkillBruno = criarSkill(tokenBruno, idBruno, "Go");
        var idGoal = criarMeta(tokenAna, idAna, idSkillAna, "Meta");

        mvc.perform(put("/goals/" + idGoal).header("Authorization", bearer(tokenAna))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(metaJson(idAna, idSkillBruno, "Meta")))
                .andExpect(status().isForbidden());
    }

    @Test
    void usuarioNaoAcessaNemAlteraProgressoDeOutroUsuario() throws Exception {
        var idSkillAna = criarSkill(tokenAna, idAna, "Java");
        var idSkillBruno = criarSkill(tokenBruno, idBruno, "Go");
        var idProgresso = criarProgresso(tokenAna, idSkillAna, 10);

        mvc.perform(get("/progress/" + idProgresso).header("Authorization", bearer(tokenBruno)))
                .andExpect(status().isForbidden());
        mvc.perform(put("/progress/" + idProgresso).header("Authorization", bearer(tokenBruno))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"skill_id\":" + idSkillBruno + ",\"percentage\":99}"))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/progress/" + idProgresso).header("Authorization", bearer(tokenBruno)))
                .andExpect(status().isForbidden());

        mvc.perform(get("/progress/" + idProgresso).header("Authorization", bearer(tokenAna)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.percentage").value(10));
    }

    @Test
    void usuarioNaoAcessaDadosDeOutroUsuarioNemRotasDeAdmin() throws Exception {
        mvc.perform(get("/usuarios/" + idAna).header("Authorization", bearer(tokenBruno)))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/usuarios/" + idAna).header("Authorization", bearer(tokenBruno)))
                .andExpect(status().isForbidden());
        mvc.perform(get("/usuarios").header("Authorization", bearer(tokenBruno)))
                .andExpect(status().isForbidden());
        mvc.perform(post("/categories").header("Authorization", bearer(tokenBruno))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Nova\"}"))
                .andExpect(status().isForbidden());

        mvc.perform(get("/usuarios/" + idBruno).header("Authorization", bearer(tokenBruno)))
                .andExpect(status().isOk());
        mvc.perform(get("/categories").header("Authorization", bearer(tokenBruno)))
                .andExpect(status().isOk());
    }

    @Test
    void usuarioNaoAlteraOsDadosDeOutroUsuario() throws Exception {
        mvc.perform(put("/usuarios/" + idAna).header("Authorization", bearer(tokenBruno))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Bruno\",\"email\":\"outro@email.com\"}"))
                .andExpect(status().isForbidden());

        assertThat(usuarioRepository.findById(idAna).orElseThrow().getEmail()).isEqualTo("ana@email.com");
    }

    @Test
    void cadastroPublicoIgnoraOPerfilInformado() throws Exception {
        mvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Eva\",\"email\":\"eva@email.com\",\"senha\":\"123456\",\"perfil\":\"ADMIN\"}"))
                .andExpect(status().isOk());

        assertThat(usuarioRepository.findByEmail("eva@email.com").orElseThrow().getPerfil().name()).isEqualTo("USER");
    }

    @Test
    void requisicaoSemTokenOuComTokenInvalidoRetorna401() throws Exception {
        mvc.perform(get("/goals"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/goals").header("Authorization", "Bearer token.invalido.aqui"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminAcessaDadosDeQualquerUsuario() throws Exception {
        var idSkill = criarSkill(tokenAna, idAna, "Java");
        var tokenAdmin = criarAdmin();

        mvc.perform(get("/api/skills/" + idSkill).header("Authorization", bearer(tokenAdmin)))
                .andExpect(status().isOk());
        mvc.perform(get("/usuarios").header("Authorization", bearer(tokenAdmin)))
                .andExpect(status().isOk());
        mvc.perform(post("/categories").header("Authorization", bearer(tokenAdmin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Nova\"}"))
                .andExpect(status().isCreated());
    }
}
