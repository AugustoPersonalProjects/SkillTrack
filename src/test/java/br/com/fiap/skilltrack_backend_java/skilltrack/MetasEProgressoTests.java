package br.com.fiap.skilltrack_backend_java.skilltrack;

import br.com.fiap.skilltrack_backend_java.skilltrack.config.ClockConfig;
import br.com.fiap.skilltrack_backend_java.skilltrack.model.Category;
import br.com.fiap.skilltrack_backend_java.skilltrack.model.Usuario;
import br.com.fiap.skilltrack_backend_java.skilltrack.model.enums.Perfil;
import br.com.fiap.skilltrack_backend_java.skilltrack.repository.CategoryRepository;
import br.com.fiap.skilltrack_backend_java.skilltrack.repository.GoalRepository;
import br.com.fiap.skilltrack_backend_java.skilltrack.repository.UsuarioRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class MetasEProgressoTests {

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
    private MockMvc mvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private GoalRepository goalRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String tokenAna;
    private Long idAna;
    private Long idCategoria;
    private Long idSkill;

    @BeforeEach
    void setUp() throws Exception {
        tokenAna = registrar("Ana", "ana@email.com");
        idAna = usuarioRepository.findByEmail("ana@email.com").orElseThrow().getId();

        var categoria = new Category();
        categoria.setName("Backend");
        idCategoria = categoryRepository.save(categoria).getId();

        idSkill = criarSkill(tokenAna, idAna, "Java");
    }

    @Test
    void prazoDeHojeEhAceitoMesmoQuandoJaEAmanhaEmUtc() throws Exception {
        mvc.perform(post("/goals").header("Authorization", bearer(tokenAna))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(metaJson("Meta", "2026-10-31")))
                .andExpect(status().isCreated());
    }

    @Test
    void prazoPassadoEhRecusadoNaCriacao() throws Exception {
        mvc.perform(post("/goals").header("Authorization", bearer(tokenAna))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(metaJson("Meta", "2026-10-30")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("A data limite deve ser hoje ou uma data futura."));
    }

    @Test
    void metaAtrasadaPodeSerEditadaSemMudarOPrazo() throws Exception {
        var idMeta = criarMetaAtrasada("2026-10-01");

        mvc.perform(put("/goals/" + idMeta).header("Authorization", bearer(tokenAna))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(metaJson("Novo título", "2026-10-01")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Novo título"))
                .andExpect(jsonPath("$.deadline").value("2026-10-01"));
    }

    @Test
    void metaAtrasadaNaoAceitaOutroPrazoPassado() throws Exception {
        var idMeta = criarMetaAtrasada("2026-10-01");

        mvc.perform(put("/goals/" + idMeta).header("Authorization", bearer(tokenAna))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(metaJson("Meta", "2026-10-15")))
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
    void adminNaoVinculaMetaASkillDeOutroUsuario() throws Exception {
        var tokenBruno = registrar("Bruno", "bruno@email.com");
        var idBruno = usuarioRepository.findByEmail("bruno@email.com").orElseThrow().getId();
        var idSkillBruno = criarSkill(tokenBruno, idBruno, "Go");
        var idMetaAna = criarMetaAtrasada("2026-10-31");
        var tokenAdmin = criarAdmin();
        var metaNaSkillDoBruno = "{\"title\":\"Meta\",\"skill_id\":" + idSkillBruno + ",\"user_id\":" + idAna + "}";

        mvc.perform(post("/goals").header("Authorization", bearer(tokenAdmin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(metaNaSkillDoBruno))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("A skill informada não pertence a este usuário."));
        mvc.perform(put("/goals/" + idMetaAna).header("Authorization", bearer(tokenAdmin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(metaNaSkillDoBruno))
                .andExpect(status().isBadRequest());
    }

    private String criarAdmin() throws Exception {
        var admin = new Usuario();
        admin.setNome("Admin");
        admin.setEmail("admin@email.com");
        admin.setSenha(passwordEncoder.encode("123456"));
        admin.setPerfil(Perfil.ADMIN);
        usuarioRepository.save(admin);

        var resposta = mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@email.com\",\"senha\":\"123456\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(resposta, "$.token");
    }

    // o prazo passado é gravado direto no banco porque a API não permite criá-lo
    private Long criarMetaAtrasada(String prazo) throws Exception {
        var resposta = mvc.perform(post("/goals").header("Authorization", bearer(tokenAna))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(metaJson("Meta", "2026-10-31")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Number idMeta = JsonPath.read(resposta, "$.id_goal");

        var meta = goalRepository.findById(idMeta.longValue()).orElseThrow();
        meta.setDeadline(LocalDate.parse(prazo));
        goalRepository.save(meta);
        return meta.getId();
    }

    private String metaJson(String titulo, String prazo) {
        return "{\"title\":\"" + titulo + "\",\"deadline\":\"" + prazo + "\",\"skill_id\":" + idSkill
                + ",\"user_id\":" + idAna + "}";
    }

    private String registrar(String nome, String email) throws Exception {
        var resposta = mvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"" + nome + "\",\"email\":\"" + email + "\",\"senha\":\"123456\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(resposta, "$.token");
    }

    private Long criarSkill(String token, Long userId, String nome) throws Exception {
        var resposta = mvc.perform(post("/api/skills").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + nome + "\",\"categoryId\":" + idCategoria
                                + ",\"level\":\"BEGINNER\",\"userId\":" + userId + "}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Number id = JsonPath.read(resposta, "$.id_skill");
        return id.longValue();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
