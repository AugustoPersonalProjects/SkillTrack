package br.com.fiap.skilltrack_backend_java.skilltrack;

import br.com.fiap.skilltrack_backend_java.skilltrack.repository.GoalRepository;
import br.com.fiap.skilltrack_backend_java.skilltrack.repository.ProgressRepository;
import br.com.fiap.skilltrack_backend_java.skilltrack.repository.SkillRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Regras que dependem das migrações: chaves estrangeiras, ON DELETE CASCADE e dados iniciais
class IntegridadeBancoTests extends TesteDeIntegracao {

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private GoalRepository goalRepository;

    @Autowired
    private ProgressRepository progressRepository;

    @Test
    void migracaoCriaAsCategoriasPadrao() throws Exception {
        var token = registrar("Ana", "ana@email.com");

        mvc.perform(get("/categories").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].name",
                        containsInAnyOrder("Backend", "Frontend", "Database", "DevOps", "Soft Skills")));
    }

    @Test
    void excluirUsuarioRemoveSkillsMetasEProgressos() throws Exception {
        var token = registrar("Ana", "ana@email.com");
        var idAna = idDoUsuario("ana@email.com");
        var idSkill = criarSkill(token, idAna, "Java");
        criarMeta(token, idAna, idSkill, "Meta");
        criarProgresso(token, idSkill, 50);

        mvc.perform(delete("/usuarios/" + idAna).header("Authorization", bearer(token)))
                .andExpect(status().isNoContent());

        assertThat(skillRepository.count()).isZero();
        assertThat(goalRepository.count()).isZero();
        assertThat(progressRepository.count()).isZero();
    }

    @Test
    void excluirSkillRemoveMetasEProgressos() throws Exception {
        var token = registrar("Ana", "ana@email.com");
        var idAna = idDoUsuario("ana@email.com");
        var idSkill = criarSkill(token, idAna, "Java");
        criarMeta(token, idAna, idSkill, "Meta");
        criarProgresso(token, idSkill, 50);

        mvc.perform(delete("/api/skills/" + idSkill).header("Authorization", bearer(token)))
                .andExpect(status().isNoContent());

        assertThat(goalRepository.count()).isZero();
        assertThat(progressRepository.count()).isZero();
    }

    @Test
    void categoriaComSkillsNaoPodeSerExcluida() throws Exception {
        var token = registrar("Ana", "ana@email.com");
        criarSkill(token, idDoUsuario("ana@email.com"), "Java");

        mvc.perform(delete("/categories/" + idCategoria).header("Authorization", bearer(criarAdmin())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("A categoria possui habilidades vinculadas e não pode ser removida."));
    }
}
