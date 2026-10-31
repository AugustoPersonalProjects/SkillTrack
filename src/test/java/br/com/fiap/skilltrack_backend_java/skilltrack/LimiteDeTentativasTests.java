package br.com.fiap.skilltrack_backend_java.skilltrack;

import br.com.fiap.skilltrack_backend_java.skilltrack.exceptions.LimiteExcedidoException;
import br.com.fiap.skilltrack_backend_java.skilltrack.model.Usuario;
import br.com.fiap.skilltrack_backend_java.skilltrack.model.enums.Perfil;
import br.com.fiap.skilltrack_backend_java.skilltrack.service.LimiteDeTentativasService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.ResultActions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Os baldes duram enquanto o contexto existir, por isso cada teste usa os próprios IPs e e-mails
@SpringBootTest(properties = {"api.rate-limit.enabled=true", "api.rate-limit.client-ip-header=CF-Connecting-IP"})
class LimiteDeTentativasTests extends TesteDeIntegracao {

    private static final String SENHA = "senhaCorreta1";

    @Test
    void emailEhBloqueadoDepoisDeCincoTentativasMesmoVindoDeOutrosIps() throws Exception {
        criarUsuario("maria@email.com");

        for (int i = 1; i <= 5; i++) {
            tentarLogin("maria@email.com", "senhaErrada", "200.0.0." + i).andExpect(status().isUnauthorized());
        }

        // nem a senha certa passa enquanto o e-mail estiver bloqueado
        tentarLogin("maria@email.com", SENHA, "200.0.0.6")
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists(HttpHeaders.RETRY_AFTER))
                .andExpect(jsonPath("$.message").value("Muitas tentativas. Tente novamente em 1 minuto."));
    }

    @Test
    void variacoesDoEmailContamParaAMesmaConta() throws Exception {
        criarUsuario("joao@email.com");
        // o MySQL ignora maiúsculas e acentos ao comparar, então todas chegam à mesma conta
        tentarLogin("JOÃO@EMAIL.COM", SENHA, "201.0.0.1").andExpect(status().isOk());

        for (var variacao : new String[]{"joao@email.com", "JOAO@email.com", "joão@email.com", "João@Email.com", "jõao@email.com"}) {
            tentarLogin(variacao, "senhaErrada", "201.0.0.2").andExpect(status().isUnauthorized());
        }

        tentarLogin("joao@email.com", "senhaErrada", "201.0.0.2").andExpect(status().isTooManyRequests());
    }

    @Test
    void loginCorretoZeraAsTentativasDoEmail() throws Exception {
        criarUsuario("carla@email.com");

        for (int i = 0; i < 4; i++) {
            tentarLogin("carla@email.com", "senhaErrada", "202.0.0.1").andExpect(status().isUnauthorized());
        }
        tentarLogin("carla@email.com", SENHA, "202.0.0.1").andExpect(status().isOk());

        for (int i = 0; i < 5; i++) {
            tentarLogin("carla@email.com", "senhaErrada", "202.0.0.1").andExpect(status().isUnauthorized());
        }
        tentarLogin("carla@email.com", "senhaErrada", "202.0.0.1").andExpect(status().isTooManyRequests());
    }

    @Test
    void ipEhBloqueadoAoTentarMuitosEmailsDiferentes() throws Exception {
        // as fichas voltam aos poucos durante o teste, por isso o bloqueio pode vir um pouco depois da 20ª tentativa
        int tentativasAceitas = 0;
        while (tentativasAceitas < 40) {
            var status = tentarLogin("pessoa" + tentativasAceitas + "@email.com", "senhaErrada", "203.0.0.1")
                    .andReturn().getResponse().getStatus();
            if (status == 429) break;
            assertThat(status).isEqualTo(401);
            tentativasAceitas++;
        }

        assertThat(tentativasAceitas).isBetween(20, 39);
        tentarLogin("pessoa-nova@email.com", "senhaErrada", "203.0.0.2").andExpect(status().isUnauthorized());
    }

    @Test
    void cadastroEhBloqueadoDepoisDeCincoContasPorIp() throws Exception {
        for (int i = 1; i <= 5; i++) {
            tentarCadastro("novo" + i + "@email.com", "204.0.0.1").andExpect(status().isOk());
        }

        tentarCadastro("novo6@email.com", "204.0.0.1")
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.message").value("Muitas tentativas. Tente novamente em 12 minutos."));

        tentarCadastro("novo6@email.com", "204.0.0.2").andExpect(status().isOk());
    }

    @Test
    void senhaAtualEhBloqueadaDepoisDeCincoErrosSemEncerrarASessao() throws Exception {
        criarUsuario("lia@email.com");
        var token = login("lia@email.com", SENHA);
        var alteracao = "{\"senha_atual\":\"senhaErrada\",\"nova_senha\":\"novaSenha1\"}";

        for (int i = 0; i < 5; i++) {
            mvc.perform(put("/usuarios/me/senha").header("Authorization", bearer(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(alteracao))
                    .andExpect(status().isBadRequest());
        }

        mvc.perform(put("/usuarios/me/email").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"outro@email.com\",\"senha_atual\":\"" + SENHA + "\"}"))
                .andExpect(status().isTooManyRequests());

        mvc.perform(get("/usuarios/me").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("lia@email.com"));
    }

    @Test
    void semCabecalhoConfiguradoOClienteNaoEscapaDoLimiteTrocandoOIpInformado() {
        var limite = new LimiteDeTentativasService(true, "");

        for (int i = 1; i <= 5; i++) {
            limite.verificarCadastro(requisicaoDoIpDeConexao("205.0.0.1", "198.51.100." + i));
        }

        assertThatThrownBy(() -> limite.verificarCadastro(requisicaoDoIpDeConexao("205.0.0.1", "198.51.100.99")))
                .isInstanceOf(LimiteExcedidoException.class);
    }

    @Test
    void semOCabecalhoNaRequisicaoUsaOIpDaConexao() {
        var limite = new LimiteDeTentativasService(true, "CF-Connecting-IP");

        for (int i = 0; i < 5; i++) {
            var request = new MockHttpServletRequest();
            request.setRemoteAddr("206.0.0.1");
            limite.verificarCadastro(request);
        }

        var outraConexao = new MockHttpServletRequest();
        outraConexao.setRemoteAddr("206.0.0.2");
        limite.verificarCadastro(outraConexao);

        var mesmaConexao = new MockHttpServletRequest();
        mesmaConexao.setRemoteAddr("206.0.0.1");
        assertThatThrownBy(() -> limite.verificarCadastro(mesmaConexao))
                .isInstanceOf(LimiteExcedidoException.class);
    }

    @Test
    void limiteDesligadoNaoBloqueia() {
        var limite = new LimiteDeTentativasService(false, "");

        for (int i = 0; i < 50; i++) {
            limite.verificarCadastro(requisicaoDoIpDeConexao("207.0.0.1", "198.51.100.1"));
        }
    }

    private MockHttpServletRequest requisicaoDoIpDeConexao(String ipDaConexao, String ipFalsificado) {
        var request = new MockHttpServletRequest();
        request.setRemoteAddr(ipDaConexao);
        request.addHeader("CF-Connecting-IP", ipFalsificado);
        request.addHeader("X-Forwarded-For", ipFalsificado);
        return request;
    }

    private void criarUsuario(String email) {
        var usuario = new Usuario();
        usuario.setNome("Pessoa");
        usuario.setEmail(email);
        usuario.setSenha(passwordEncoder.encode(SENHA));
        usuario.setPerfil(Perfil.USER);
        usuarioRepository.save(usuario);
    }

    private ResultActions tentarLogin(String email, String senha, String ip) throws Exception {
        return mvc.perform(post("/auth/login").header("CF-Connecting-IP", ip)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"senha\":\"" + senha + "\"}"));
    }

    private ResultActions tentarCadastro(String email, String ip) throws Exception {
        return mvc.perform(post("/auth/register").header("CF-Connecting-IP", ip)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Pessoa\",\"email\":\"" + email + "\",\"senha\":\"123456\"}"));
    }
}
