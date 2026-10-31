package br.com.fiap.skilltrack_backend_java.skilltrack;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MeuPerfilTests extends TesteDeIntegracao {

    private String tokenAna;
    private Long idAna;

    @BeforeEach
    void setUp() throws Exception {
        tokenAna = registrar("Ana", "ana@email.com");
        idAna = idDoUsuario("ana@email.com");
    }

    @Test
    void usuarioLogadoConsultaOsProprioDados() throws Exception {
        registrar("Bruno", "bruno@email.com");

        mvc.perform(get("/usuarios/me").header("Authorization", bearer(tokenAna)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id_usuario").value(idAna))
                .andExpect(jsonPath("$.email").value("ana@email.com"))
                .andExpect(jsonPath("$.bio").isEmpty())
                .andExpect(jsonPath("$.cor_avatar").value("ESMERALDA"))
                .andExpect(jsonPath("$.tema").value("SISTEMA"))
                .andExpect(jsonPath("$.senha").doesNotExist())
                .andExpect(jsonPath("$.versaoToken").doesNotExist());
    }

    @Test
    void usuarioPersonalizaOPerfil() throws Exception {
        mvc.perform(put("/usuarios/me").header("Authorization", bearer(tokenAna))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"  Ana Souza  \",\"bio\":\"  Dev Java  \",\"cor_avatar\":\"VIOLETA\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Ana Souza"))
                .andExpect(jsonPath("$.bio").value("Dev Java"))
                .andExpect(jsonPath("$.cor_avatar").value("VIOLETA"));

        mvc.perform(get("/usuarios/me").header("Authorization", bearer(tokenAna)))
                .andExpect(jsonPath("$.nome").value("Ana Souza"))
                .andExpect(jsonPath("$.cor_avatar").value("VIOLETA"));
    }

    @Test
    void bioEmBrancoLimpaOCampo() throws Exception {
        mvc.perform(put("/usuarios/me").header("Authorization", bearer(tokenAna))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Ana\",\"bio\":\"Dev Java\",\"cor_avatar\":\"AZUL\"}"))
                .andExpect(jsonPath("$.bio").value("Dev Java"));

        mvc.perform(put("/usuarios/me").header("Authorization", bearer(tokenAna))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Ana\",\"bio\":\"   \",\"cor_avatar\":\"AZUL\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bio").isEmpty());
    }

    @Test
    void personalizarOPerfilNaoAlteraEmailSenhaNemPerfilDeAcesso() throws Exception {
        mvc.perform(put("/usuarios/me").header("Authorization", bearer(tokenAna))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Ana\",\"cor_avatar\":\"ROSA\",\"email\":\"outro@email.com\","
                                + "\"senha\":\"invasora1\",\"perfil\":\"ADMIN\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ana@email.com"))
                .andExpect(jsonPath("$.perfil").value("USER"));

        login("ana@email.com", "123456");
    }

    @Test
    void dadosInvalidosDoPerfilSaoRecusados() throws Exception {
        mvc.perform(put("/usuarios/me").header("Authorization", bearer(tokenAna))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Ana\",\"cor_avatar\":\"DOURADO\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("O valor enviado para 'cor_avatar' é inválido."));
        mvc.perform(put("/usuarios/me").header("Authorization", bearer(tokenAna))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Ana\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].mensagem").value("Escolha a cor do avatar"));
        mvc.perform(put("/usuarios/me").header("Authorization", bearer(tokenAna))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Ana\",\"cor_avatar\":\"AZUL\",\"bio\":\"" + "a".repeat(281) + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].mensagem").value("A bio deve ter no máximo 280 caracteres"));
    }

    @Test
    void bioAceitaOTamanhoMaximoEAcentos() throws Exception {
        var bio = "é".repeat(280);

        mvc.perform(put("/usuarios/me").header("Authorization", bearer(tokenAna))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Ana\",\"cor_avatar\":\"AZUL\",\"bio\":\"" + bio + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bio").value(bio));
    }

    @Test
    void temaFicaSalvoNaContaEVaiParaOutrasSessoes() throws Exception {
        var outroDispositivo = login("ana@email.com", "123456");

        mvc.perform(put("/usuarios/me/tema").header("Authorization", bearer(tokenAna))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tema\":\"ESCURO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tema").value("ESCURO"));

        mvc.perform(get("/usuarios/me").header("Authorization", bearer(outroDispositivo)))
                .andExpect(jsonPath("$.tema").value("ESCURO"));
    }

    @Test
    void temaInvalidoEhRecusado() throws Exception {
        mvc.perform(put("/usuarios/me/tema").header("Authorization", bearer(tokenAna))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tema\":\"AZUL\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("O valor enviado para 'tema' é inválido."));
        mvc.perform(put("/usuarios/me/tema").header("Authorization", bearer(tokenAna))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].mensagem").value("Escolha o tema"));
    }

    @Test
    void usuarioNaoAlteraOPerfilDeOutraPessoaPorEstasRotas() throws Exception {
        registrar("Bruno", "bruno@email.com");

        mvc.perform(put("/usuarios/me").header("Authorization", bearer(tokenAna))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Nome Novo\",\"cor_avatar\":\"AZUL\",\"id_usuario\":"
                                + idDoUsuario("bruno@email.com") + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id_usuario").value(idAna));

        assertThat(usuarioRepository.findByEmail("bruno@email.com").orElseThrow().getNome()).isEqualTo("Bruno");
    }

    @Test
    void rotasDoPerfilExigemToken() throws Exception {
        mvc.perform(get("/usuarios/me"))
                .andExpect(status().isUnauthorized());
        mvc.perform(put("/usuarios/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Ana\",\"cor_avatar\":\"AZUL\"}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(put("/usuarios/me/tema")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tema\":\"ESCURO\"}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(put("/usuarios/me/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(alteracaoEmail("novo@email.com", "123456")))
                .andExpect(status().isUnauthorized());
        mvc.perform(put("/usuarios/me/senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(alteracaoSenha("123456", "novaSenha1")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void alterarOEmailExigeASenhaAtual() throws Exception {
        mvc.perform(put("/usuarios/me/email").header("Authorization", bearer(tokenAna))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(alteracaoEmail("novo@email.com", "senhaErrada")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("A senha atual está incorreta."));
        mvc.perform(put("/usuarios/me/email").header("Authorization", bearer(tokenAna))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"novo@email.com\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].mensagem").value("Informe a senha atual"));

        assertThat(usuarioRepository.findById(idAna).orElseThrow().getEmail()).isEqualTo("ana@email.com");
    }

    @Test
    void alterarOEmailMantemASessaoEPermiteEntrarComONovo() throws Exception {
        mvc.perform(put("/usuarios/me/email").header("Authorization", bearer(tokenAna))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(alteracaoEmail("novo@email.com", "123456")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("novo@email.com"));

        mvc.perform(get("/usuarios/me").header("Authorization", bearer(tokenAna)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("novo@email.com"));
        login("novo@email.com", "123456");
    }

    @Test
    void emailDeOutraContaNaoPodeSerUsado() throws Exception {
        registrar("Bruno", "bruno@email.com");

        mvc.perform(put("/usuarios/me/email").header("Authorization", bearer(tokenAna))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(alteracaoEmail("bruno@email.com", "123456")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("E-mail já cadastrado."));
    }

    @Test
    void emailInvalidoEhRecusado() throws Exception {
        mvc.perform(put("/usuarios/me/email").header("Authorization", bearer(tokenAna))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(alteracaoEmail("nao-e-email", "123456")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].mensagem").value("Informe um e-mail válido"));
    }

    @Test
    void alterarASenhaExigeASenhaAtual() throws Exception {
        mvc.perform(put("/usuarios/me/senha").header("Authorization", bearer(tokenAna))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(alteracaoSenha("senhaErrada", "novaSenha1")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("A senha atual está incorreta."));

        // a senha errada não pode encerrar a sessão de quem digitou
        mvc.perform(get("/usuarios/me").header("Authorization", bearer(tokenAna)))
                .andExpect(status().isOk());
        login("ana@email.com", "123456");
    }

    @Test
    void novaSenhaCurtaEhRecusada() throws Exception {
        mvc.perform(put("/usuarios/me/senha").header("Authorization", bearer(tokenAna))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(alteracaoSenha("123456", "123")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].mensagem").value("A senha deve ter entre 6 e 72 caracteres"));
    }

    @Test
    void alterarASenhaEncerraAsOutrasSessoesEDevolveUmNovoToken() throws Exception {
        var outraSessao = login("ana@email.com", "123456");

        var resposta = mvc.perform(put("/usuarios/me/senha").header("Authorization", bearer(tokenAna))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(alteracaoSenha("123456", "novaSenha1")))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String tokenNovo = JsonPath.read(resposta, "$.token");

        mvc.perform(get("/usuarios/me").header("Authorization", bearer(outraSessao)))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/usuarios/me").header("Authorization", bearer(tokenAna)))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/usuarios/me").header("Authorization", bearer(tokenNovo)))
                .andExpect(status().isOk());

        login("ana@email.com", "novaSenha1");
        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ana@email.com\",\"senha\":\"123456\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void novaSenhaComMaisDe72BytesEhRecusadaSemAlterarASenha() throws Exception {
        // 40 caracteres, mas 80 bytes: cada "ç" ocupa dois
        mvc.perform(put("/usuarios/me/senha").header("Authorization", bearer(tokenAna))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(alteracaoSenha("123456", "ç".repeat(40))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("A senha é longa demais. Use menos caracteres, principalmente acentos e emojis."));

        mvc.perform(get("/usuarios/me").header("Authorization", bearer(tokenAna)))
                .andExpect(status().isOk());
    }

    private String alteracaoEmail(String email, String senhaAtual) {
        return "{\"email\":\"" + email + "\",\"senha_atual\":\"" + senhaAtual + "\"}";
    }

    private String alteracaoSenha(String senhaAtual, String novaSenha) {
        return "{\"senha_atual\":\"" + senhaAtual + "\",\"nova_senha\":\"" + novaSenha + "\"}";
    }
}
