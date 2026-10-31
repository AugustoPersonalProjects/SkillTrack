package br.com.fiap.skilltrack_backend_java.skilltrack;

import br.com.fiap.skilltrack_backend_java.skilltrack.model.Category;
import br.com.fiap.skilltrack_backend_java.skilltrack.repository.CategoryRepository;
import br.com.fiap.skilltrack_backend_java.skilltrack.repository.UsuarioRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class DashboardTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private String tokenAna;
    private Long idAna;
    private Long idCategoria;

    @BeforeEach
    void setUp() throws Exception {
        tokenAna = registrar("Ana", "ana@email.com");
        idAna = usuarioRepository.findByEmail("ana@email.com").orElseThrow().getId();

        var categoria = new Category();
        categoria.setName("Backend");
        idCategoria = categoryRepository.save(categoria).getId();
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
        var idBruno = usuarioRepository.findByEmail("bruno@email.com").orElseThrow().getId();
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
        var prazo = LocalDate.now().plusDays(1);
        for (int i = 0; i < 5; i++) {
            var idConcluida = criarMeta(idJava, "Concluída " + i, prazo);
            mvc.perform(post("/goals/" + idConcluida + "/complete").header("Authorization", bearer(tokenAna)))
                    .andExpect(status().isNoContent());
        }
        criarMeta(idJava, "Pendente", prazo.plusDays(30));

        mvc.perform(get("/dashboard").header("Authorization", bearer(tokenAna)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalGoals").value(6))
                .andExpect(jsonPath("$.goals[*].title", contains("Pendente")));
    }

    private Number criarMeta(Long skillId, String titulo, LocalDate prazo) throws Exception {
        var resposta = mvc.perform(post("/goals").header("Authorization", bearer(tokenAna))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"" + titulo + "\",\"deadline\":\"" + prazo + "\",\"skill_id\":" + skillId
                                + ",\"user_id\":" + idAna + "}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(resposta, "$.id_goal");
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

    private void registrarProgresso(String token, Long skillId, int porcentagem, String data) throws Exception {
        mvc.perform(post("/progress").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"skill_id\":" + skillId + ",\"percentage\":" + porcentagem + ",\"date\":\"" + data + "\"}"))
                .andExpect(status().isCreated());
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
