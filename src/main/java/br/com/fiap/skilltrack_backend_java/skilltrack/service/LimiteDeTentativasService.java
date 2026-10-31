package br.com.fiap.skilltrack_backend_java.skilltrack.service;

import br.com.fiap.skilltrack_backend_java.skilltrack.exceptions.LimiteExcedidoException;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.Normalizer;
import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

// Dificulta a força bruta contra senhas e os cadastros em massa. Cada chave (IP, e-mail ou usuário)
// tem um balde de fichas: cada tentativa gasta uma, e elas voltam aos poucos.
// Os baldes ficam na memória, o que basta para uma instância; com várias, precisariam ser compartilhados (ex.: Redis)
@Service
public class LimiteDeTentativasService {

    private static final Logger log = LoggerFactory.getLogger(LimiteDeTentativasService.class);

    private static final Bandwidth LOGIN_POR_IP = limite(20, Duration.ofMinutes(1));
    private static final Bandwidth LOGIN_POR_EMAIL = limite(5, Duration.ofMinutes(5));
    private static final Bandwidth CADASTRO_POR_IP = limite(5, Duration.ofHours(1));
    private static final Bandwidth SENHA_ATUAL_POR_USUARIO = limite(5, Duration.ofMinutes(5));

    // depois de 1 hora sem uso qualquer balde já estaria cheio, então pode ser descartado
    private final Cache<String, Bucket> baldes = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterAccess(Duration.ofHours(1))
            .build();

    private final boolean ativo;
    private final String cabecalhoDoIp;

    public LimiteDeTentativasService(@Value("${api.rate-limit.enabled}") boolean ativo,
                                     @Value("${api.rate-limit.client-ip-header}") String cabecalhoDoIp) {
        this.ativo = ativo;
        this.cabecalhoDoIp = cabecalhoDoIp;
    }

    public void verificarLogin(HttpServletRequest request, String email) {
        // o IP vem primeiro: quem já foi bloqueado não gasta as tentativas do dono do e-mail
        consumir("login-ip:" + ipDoCliente(request), LOGIN_POR_IP);
        consumir(chaveDoEmail(email), LOGIN_POR_EMAIL);
    }

    // depois de um login correto, os erros de digitação anteriores não contam mais contra o dono da conta
    public void liberarLogin(String email) {
        baldes.invalidate(chaveDoEmail(email));
    }

    public void verificarCadastro(HttpServletRequest request) {
        consumir("cadastro-ip:" + ipDoCliente(request), CADASTRO_POR_IP);
    }

    public void verificarSenhaAtual(Long idUsuario) {
        consumir("senha-atual:" + idUsuario, SENHA_ATUAL_POR_USUARIO);
    }

    private void consumir(String chave, Bandwidth limite) {
        if (!ativo) return;

        var balde = baldes.get(chave, novaChave -> Bucket.builder().addLimit(limite).build());
        var consumo = balde.tryConsumeAndReturnRemaining(1);
        if (!consumo.isConsumed()) {
            log.warn("Limite de tentativas excedido: {}", chave.substring(0, chave.indexOf(':')));
            throw new LimiteExcedidoException(TimeUnit.NANOSECONDS.toSeconds(consumo.getNanosToWaitForRefill()) + 1);
        }
    }

    // atrás de um proxy, o IP da conexão é o do proxy, e todos os clientes dividiriam o mesmo balde
    private String ipDoCliente(HttpServletRequest request) {
        var ipInformado = StringUtils.hasText(cabecalhoDoIp) ? request.getHeader(cabecalhoDoIp) : null;
        return StringUtils.hasText(ipInformado) ? ipInformado.trim() : request.getRemoteAddr();
    }

    // o MySQL compara e-mails ignorando maiúsculas e acentos, e cada variação precisa cair no mesmo balde
    private String chaveDoEmail(String email) {
        var semAcentos = Normalizer.normalize(email, Normalizer.Form.NFKD).replaceAll("\\p{M}", "");
        return "login-email:" + semAcentos.toLowerCase(Locale.ROOT);
    }

    private static Bandwidth limite(int tentativas, Duration periodo) {
        return Bandwidth.builder().capacity(tentativas).refillGreedy(tentativas, periodo).build();
    }
}
