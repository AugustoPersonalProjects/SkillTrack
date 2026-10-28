package br.com.fiap.skilltrack_backend_java.skilltrack;

import br.com.fiap.skilltrack_backend_java.skilltrack.model.Category;
import br.com.fiap.skilltrack_backend_java.skilltrack.model.Usuario;
import br.com.fiap.skilltrack_backend_java.skilltrack.model.enums.Perfil;
import br.com.fiap.skilltrack_backend_java.skilltrack.repository.CategoryRepository;
import br.com.fiap.skilltrack_backend_java.skilltrack.repository.UsuarioRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AutorizacaoDonoDadosTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String tokenAna;
    private String tokenBruno;
    private Long idAna;
    private Long idBruno;
    private Long idCategoria;

    @BeforeEach
    void setUp() throws Exception {
        tokenAna = registrar("Ana", "ana@email.com");
        tokenBruno = registrar("Bruno", "bruno@email.com");
        idAna = usuarioRepository.findByEmail("ana@email.com").orElseThrow().getId();
        idBruno = usuarioRepository.findByEmail("bruno@email.com").orElseThrow().getId();

        var categoria = new Category();
        categoria.setName("Backend");
        idCategoria = categoryRepository.save(categoria).getId();
    }

    @Test
    void usuarioNaoAcessaSkillsDeOutroUsuario() throws Exception {
        var idSkill = criarSkill(tokenAna, idAna);

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
    void usuarioNaoCriaRegistrosEmNomeDeOutroUsuario() throws Exception {
        var idSkillAna = criarSkill(tokenAna, idAna);

        mvc.perform(post("/api/skills").header("Authorization", bearer(tokenBruno))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(skillJson(idAna)))
                .andExpect(status().isForbidden());

        // meta do Bruno vinculada à skill da Ana
        mvc.perform(post("/goals").header("Authorization", bearer(tokenBruno))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Meta\",\"skill_id\":" + idSkillAna + ",\"user_id\":" + idBruno + "}"))
                .andExpect(status().isForbidden());

        mvc.perform(post("/progress").header("Authorization", bearer(tokenBruno))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"skill_id\":" + idSkillAna + ",\"percentage\":50}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void usuarioNaoAcessaMetasDeOutroUsuario() throws Exception {
        var idSkill = criarSkill(tokenAna, idAna);
        var resposta = mvc.perform(post("/goals").header("Authorization", bearer(tokenAna))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Meta\",\"skill_id\":" + idSkill + ",\"user_id\":" + idAna + "}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Number idGoal = JsonPath.read(resposta, "$.id_goal");

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
    void adminAcessaDadosDeQualquerUsuario() throws Exception {
        var idSkill = criarSkill(tokenAna, idAna);

        var admin = new Usuario();
        admin.setNome("Admin");
        admin.setEmail("admin@email.com");
        admin.setSenha(passwordEncoder.encode("123456"));
        admin.setPerfil(Perfil.ADMIN);
        usuarioRepository.save(admin);
        var tokenAdmin = login("admin@email.com");

        mvc.perform(get("/api/skills/" + idSkill).header("Authorization", bearer(tokenAdmin)))
                .andExpect(status().isOk());
        mvc.perform(get("/usuarios").header("Authorization", bearer(tokenAdmin)))
                .andExpect(status().isOk());
        mvc.perform(post("/categories").header("Authorization", bearer(tokenAdmin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Nova\"}"))
                .andExpect(status().isCreated());
    }

    private String registrar(String nome, String email) throws Exception {
        var resposta = mvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"" + nome + "\",\"email\":\"" + email + "\",\"senha\":\"123456\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(resposta, "$.token");
    }

    private String login(String email) throws Exception {
        var resposta = mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"senha\":\"123456\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(resposta, "$.token");
    }

    private Long criarSkill(String token, Long userId) throws Exception {
        var resposta = mvc.perform(post("/api/skills").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(skillJson(userId)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Number id = JsonPath.read(resposta, "$.id_skill");
        return id.longValue();
    }

    private String skillJson(Long userId) {
        return "{\"name\":\"Java\",\"categoryId\":" + idCategoria + ",\"level\":\"BEGINNER\",\"userId\":" + userId + "}";
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
