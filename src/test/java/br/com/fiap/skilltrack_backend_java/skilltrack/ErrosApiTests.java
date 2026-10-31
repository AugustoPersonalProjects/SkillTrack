package br.com.fiap.skilltrack_backend_java.skilltrack;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ErrosApiTests {

    @Autowired
    private MockMvc mvc;

    private String tokenAna;

    @BeforeEach
    void setUp() throws Exception {
        var resposta = mvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Ana\",\"email\":\"ana@email.com\",\"senha\":\"123456\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        tokenAna = JsonPath.read(resposta, "$.token");
    }

    @Test
    void metodoNaoSuportadoRetorna405() throws Exception {
        mvc.perform(patch("/goals/1").header("Authorization", "Bearer " + tokenAna))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(header().exists("Allow"))
                .andExpect(jsonPath("$.message").value("Esta rota não aceita a requisição enviada."));
    }

    @Test
    void formatoDoCorpoNaoSuportadoRetorna415() throws Exception {
        mvc.perform(post("/auth/login").contentType(MediaType.TEXT_PLAIN).content("texto"))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void ordenacaoPorCampoInexistenteRetorna400() throws Exception {
        mvc.perform(get("/goals?sort=campoInexistente,asc").header("Authorization", "Bearer " + tokenAna))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("O campo de ordenação informado não existe."));
    }
}
