package br.com.fiap.skilltrack_backend_java.skilltrack.service;

import br.com.fiap.skilltrack_backend_java.skilltrack.model.Usuario;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class TokenService {

    private static final String ISSUER = "API SkillTrack";
    private static final String CLAIM_VERSAO = "versao";

    @Value("${api.security.token.secret}")
    private String secret;

    public record DadosToken(Long idUsuario, int versao) {}

    public String gerarToken(Usuario usuario) {
        try {
            var algoritmo = Algorithm.HMAC256(secret);
            return JWT.create()
                    .withIssuer(ISSUER)
                    .withSubject(String.valueOf(usuario.getId()))
                    .withClaim("id", usuario.getId())
                    .withClaim(CLAIM_VERSAO, usuario.getVersaoToken())
                    .withExpiresAt(dataExpiracao())
                    .sign(algoritmo);
        } catch (JWTCreationException exception){
            throw new RuntimeException("Erro ao gerar token jwt ", exception);
        }
    }

    // null quando o token é inválido, expirado ou do formato antigo, que usava o e-mail como subject
    public DadosToken lerTokenOuNull(String tokenJWT) {
        try {
            var algoritmo = Algorithm.HMAC256(secret);
            var token = JWT.require(algoritmo)
                    .withIssuer(ISSUER)
                    .withClaimPresence(CLAIM_VERSAO)
                    .build()
                    .verify(tokenJWT);
            return new DadosToken(Long.valueOf(token.getSubject()), token.getClaim(CLAIM_VERSAO).asInt());
        } catch (JWTVerificationException | NumberFormatException exception) {
            return null;
        }
    }

    private Instant dataExpiracao() {
        return Instant.now().plus(7, ChronoUnit.DAYS);
    }

}
