package br.com.fiap.skilltrack_backend_java.skilltrack;

import br.com.fiap.skilltrack_backend_java.skilltrack.config.AdminInitializer;
import br.com.fiap.skilltrack_backend_java.skilltrack.model.enums.Perfil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// O ADMIN inicial vem das propriedades api.admin.*, como ADMIN_EMAIL e ADMIN_PASSWORD em produção
@SpringBootTest(properties = {"api.admin.email=admin@email.com", "api.admin.password=admin123"})
class AdminTests extends TesteDeIntegracao {

    @Autowired
    private AdminInitializer adminInitializer;

    private String tokenAdmin;
    private String tokenAna;
    private Long idAdmin;
    private Long idAna;

    @BeforeEach
    void setUp() throws Exception {
        // o contexto sobe uma vez só e cada teste limpa o banco, então a inicialização é repetida aqui
        adminInitializer.run(null);

        tokenAdmin = login("admin@email.com", "admin123");
        tokenAna = registrar("Ana", "ana@email.com");
        idAdmin = idDoUsuario("admin@email.com");
        idAna = idDoUsuario("ana@email.com");
    }

    @Test
    void adminInicialEhCriadoPelasPropriedades() {
        assertThat(usuarioRepository.findByEmail("admin@email.com").orElseThrow().getPerfil())
                .isEqualTo(Perfil.ADMIN);
    }

    @Test
    void resumoTrazTotaisEContagensPorUsuario() throws Exception {
        var idSkill = criarSkill(tokenAna, idAna, "Java");
        criarMeta(tokenAna, idAna, idSkill, "Estudar");

        mvc.perform(get("/admin/resumo").header("Authorization", bearer(tokenAdmin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsuarios").value(2))
                .andExpect(jsonPath("$.totalAdmins").value(1))
                .andExpect(jsonPath("$.totalSkills").value(1))
                .andExpect(jsonPath("$.totalGoals").value(1))
                .andExpect(jsonPath("$.completedGoals").value(0))
                .andExpect(jsonPath("$.usuarios[?(@.email == 'ana@email.com')].totalSkills").value(1))
                .andExpect(jsonPath("$.usuarios[?(@.email == 'ana@email.com')].totalGoals").value(1))
                .andExpect(jsonPath("$.usuarios[?(@.email == 'admin@email.com')].totalSkills").value(0));
    }

    @Test
    void usuarioComumNaoAcessaOResumo() throws Exception {
        mvc.perform(get("/admin/resumo").header("Authorization", bearer(tokenAna)))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminAlteraPerfilSemInformarSenha() throws Exception {
        mvc.perform(put("/usuarios/" + idAna).header("Authorization", bearer(tokenAdmin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Ana Souza\",\"email\":\"ana@email.com\",\"perfil\":\"ADMIN\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.perfil").value("ADMIN"))
                .andExpect(jsonPath("$.nome").value("Ana Souza"));

        // a senha antiga continua valendo
        login("ana@email.com", "123456");
    }

    @Test
    void usuarioComumNaoPromoveASiMesmo() throws Exception {
        mvc.perform(put("/usuarios/" + idAna).header("Authorization", bearer(tokenAna))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Ana\",\"email\":\"ana@email.com\",\"perfil\":\"ADMIN\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.perfil").value("USER"));
    }

    @Test
    void ultimoAdminNaoPodeSerRebaixadoNemExcluido() throws Exception {
        mvc.perform(put("/usuarios/" + idAdmin).header("Authorization", bearer(tokenAdmin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Admin\",\"email\":\"admin@email.com\",\"perfil\":\"USER\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Não é possível remover o último administrador do sistema."));
        mvc.perform(delete("/usuarios/" + idAdmin).header("Authorization", bearer(tokenAdmin)))
                .andExpect(status().isBadRequest());

        // com um segundo ADMIN, o primeiro pode ser rebaixado
        mvc.perform(put("/usuarios/" + idAna).header("Authorization", bearer(tokenAdmin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Ana\",\"email\":\"ana@email.com\",\"perfil\":\"ADMIN\"}"))
                .andExpect(status().isOk());
        mvc.perform(put("/usuarios/" + idAdmin).header("Authorization", bearer(tokenAdmin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Admin\",\"email\":\"admin@email.com\",\"perfil\":\"USER\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void senhaCurtaNaAtualizacaoEhRejeitada() throws Exception {
        mvc.perform(put("/usuarios/" + idAna).header("Authorization", bearer(tokenAna))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Ana\",\"email\":\"ana@email.com\",\"senha\":\"123\"}"))
                .andExpect(status().isBadRequest());
    }
}
