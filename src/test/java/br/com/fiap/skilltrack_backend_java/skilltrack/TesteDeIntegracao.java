package br.com.fiap.skilltrack_backend_java.skilltrack;

import br.com.fiap.skilltrack_backend_java.skilltrack.model.Usuario;
import br.com.fiap.skilltrack_backend_java.skilltrack.model.enums.Perfil;
import br.com.fiap.skilltrack_backend_java.skilltrack.repository.CategoryRepository;
import br.com.fiap.skilltrack_backend_java.skilltrack.repository.UsuarioRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Base dos testes: API completa, com as migrações e a configuração de produção, em um MySQL real.
// O container é único e o contexto do Spring é reaproveitado entre as classes, por isso cada teste limpa o banco.
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
abstract class TesteDeIntegracao {

    // ids das categorias padrão criadas pela migração V2, que os testes mantêm
    private static final int ULTIMA_CATEGORIA_DA_MIGRACAO = 5;

    @ServiceConnection
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.4")
            .withUrlParam("serverTimezone", "UTC");

    static {
        MYSQL.start();
    }

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected UsuarioRepository usuarioRepository;

    @Autowired
    protected CategoryRepository categoryRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    protected Long idCategoria;

    @BeforeEach
    void limparBanco() {
        jdbcTemplate.update("DELETE FROM st_progress");
        jdbcTemplate.update("DELETE FROM st_goals");
        jdbcTemplate.update("DELETE FROM st_skills");
        jdbcTemplate.update("DELETE FROM st_usuarios");
        jdbcTemplate.update("DELETE FROM st_categories WHERE id_category > ?", ULTIMA_CATEGORIA_DA_MIGRACAO);

        idCategoria = categoryRepository.findAll().getFirst().getId();
    }

    protected String registrar(String nome, String email) throws Exception {
        var resposta = mvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"" + nome + "\",\"email\":\"" + email + "\",\"senha\":\"123456\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(resposta, "$.token");
    }

    protected String login(String email, String senha) throws Exception {
        var resposta = mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"senha\":\"" + senha + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(resposta, "$.token");
    }

    protected String criarAdmin() throws Exception {
        var admin = new Usuario();
        admin.setNome("Admin");
        admin.setEmail("admin@email.com");
        admin.setSenha(passwordEncoder.encode("123456"));
        admin.setPerfil(Perfil.ADMIN);
        usuarioRepository.save(admin);
        return login("admin@email.com", "123456");
    }

    protected Long idDoUsuario(String email) {
        return usuarioRepository.findByEmail(email).orElseThrow().getId();
    }

    protected Long criarSkill(String token, Long userId, String nome) throws Exception {
        var resposta = mvc.perform(post("/api/skills").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(skillJson(userId, nome)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Number id = JsonPath.read(resposta, "$.id_skill");
        return id.longValue();
    }

    protected Long criarMeta(String token, Long userId, Long skillId, String titulo) throws Exception {
        var resposta = mvc.perform(post("/goals").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(metaJson(userId, skillId, titulo)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Number id = JsonPath.read(resposta, "$.id_goal");
        return id.longValue();
    }

    protected Long criarProgresso(String token, Long skillId, int porcentagem) throws Exception {
        var resposta = mvc.perform(post("/progress").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"skill_id\":" + skillId + ",\"percentage\":" + porcentagem + "}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Number id = JsonPath.read(resposta, "$.id_progress");
        return id.longValue();
    }

    protected String skillJson(Long userId, String nome) {
        return "{\"name\":\"" + nome + "\",\"categoryId\":" + idCategoria
                + ",\"level\":\"BEGINNER\",\"userId\":" + userId + "}";
    }

    protected String metaJson(Long userId, Long skillId, String titulo) {
        return "{\"title\":\"" + titulo + "\",\"skill_id\":" + skillId + ",\"user_id\":" + userId + "}";
    }

    protected String bearer(String token) {
        return "Bearer " + token;
    }
}
