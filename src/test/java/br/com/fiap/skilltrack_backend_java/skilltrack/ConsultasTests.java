package br.com.fiap.skilltrack_backend_java.skilltrack;

import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.jpa.properties.hibernate.generate_statistics=true",
        "logging.level.org.hibernate.engine.internal.StatisticalLoggingSessionEventListener=WARN"
})
class ConsultasTests extends TesteDeIntegracao {

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Test
    void listarSkillsNaoFazUmaConsultaPorSkill() throws Exception {
        var token = registrar("Ana", "ana@email.com");
        var idAna = idDoUsuario("ana@email.com");
        for (int i = 0; i < 10; i++) {
            criarProgresso(token, criarSkill(token, idAna, "Skill " + i), 50);
        }

        var estatisticas = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        estatisticas.clear();

        mvc.perform(get("/api/skills/user/" + idAna).header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(10));

        // usuário logado, skills, progressos em lote e metas em lote
        assertThat(estatisticas.getPrepareStatementCount()).isLessThanOrEqualTo(4);
    }

    @Test
    void painelDeAdminNaoFazUmaConsultaPorRegistro() throws Exception {
        var token = registrar("Ana", "ana@email.com");
        var idAna = idDoUsuario("ana@email.com");
        for (int i = 0; i < 10; i++) {
            var idSkill = criarSkill(token, idAna, "Skill " + i);
            criarProgresso(token, idSkill, 50);
            criarMeta(token, idAna, idSkill, "Meta " + i);
        }
        var tokenAdmin = criarAdmin();

        var estatisticas = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        estatisticas.clear();

        mvc.perform(get("/admin/dashboard").header("Authorization", bearer(tokenAdmin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.indicadores.totalSkills").value(10));

        // usuário logado e uma consulta agregada para cada indicador e gráfico
        assertThat(estatisticas.getPrepareStatementCount()).isLessThanOrEqualTo(19);
    }
}
