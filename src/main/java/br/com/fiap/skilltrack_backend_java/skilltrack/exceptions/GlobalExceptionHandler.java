package br.com.fiap.skilltrack_backend_java.skilltrack.exceptions;

import org.apache.coyote.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 404 - Recurso não encontrado
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<?> tratarErro404(EntityNotFoundException ex){
        var body = Map.of(
                "error", "Recurso não encontrado",
                "message", ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    // 400 - Bad Request customizado
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<?> tratarBadRequest(BadRequestException ex) {
        var body = Map.of(
                "error", "Requisição inválida",
                "message", ex.getMessage()
        );
        return ResponseEntity.badRequest().body(body);
    }

    // 409 - Email já cadastrado
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> tratarDuplicidade(DataIntegrityViolationException ex) {
        String msg = String.valueOf(ex.getMostSpecificCause().getMessage()).toLowerCase();
        String detalhe;

        if (msg.contains("st_usuarios_email_uq")) {
            detalhe = "Já existe um usuário cadastrado com este e-mail.";
        } else if (msg.contains("st_skills_category_fk")) {
            detalhe = "A categoria possui habilidades vinculadas e não pode ser removida.";
        } else {
            detalhe = "Violação de integridade no banco de dados.";
        }

        var body = Map.of(
                "error", "Violação de integridade",
                "message", detalhe
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    // 400 - Erro de parâmetro
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<?> tratarErroTipoDeParametro() {
        return ResponseEntity.badRequest().body(Map.of("error", "Parâmetro inválido"));
    }

    // 400 - Erro de validação
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> tratarErro400(MethodArgumentNotValidException ex){
        var erros = ex.getFieldErrors()
                .stream()
                .map(DadosErroValidacao::new)
                .toList();

        return ResponseEntity.badRequest().body(erros);
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<?> tratarRegraNegocio(RegraNegocioException ex) {
        var body = Map.of(
                "error", "Erro de regra de negócio",
                "message", ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> tratarArgumentoInvalido(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of(
                "error", "Requisição inválida",
                "message", ex.getMessage()
        ));
    }

    // 401 - E-mail/senha incorretos
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<?> tratarErroAutenticacao(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "error", "Não autorizado",
                "message", "E-mail ou senha inválidos."
        ));
    }

    // 403 - Sem permissão
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> tratarAcessoNegado(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                "error", "Acesso negado",
                "message", "Você não tem permissão para acessar este recurso."
        ));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> tratarErroDeConversao(HttpMessageNotReadableException ex) {
        Throwable causa = ex.getCause();

        if (causa instanceof InvalidFormatException ife && !ife.getPath().isEmpty()) {
            String nomeCampo = ife.getPath().get(0).getFieldName();
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Requisição inválida",
                    "message", "O valor enviado para '" + nomeCampo + "' é inválido."
            ));
        }

        return ResponseEntity.badRequest().body(Map.of(
                "error", "Requisição inválida",
                "message", "O corpo da requisição está mal formatado."
        ));
    }

    // 404 - Url não encontrada
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<?> tratarUrlNaoEncontrada(NoHandlerFoundException ex) {
        var body = Map.of(
                "error", "Rota não encontrada",
                "message", "A URL '" + ex.getRequestURL() + "' não existe."
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    // 400 - Campo inexistente no parâmetro ?sort= das listagens
    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<?> tratarOrdenacaoInvalida() {
        return ResponseEntity.badRequest().body(Map.of(
                "error", "Requisição inválida",
                "message", "O campo de ordenação informado não existe."
        ));
    }

    // 500 - Erro genérico inesperado
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> tratarErro500(Exception ex) {
        // os erros de requisição do Spring MVC já trazem o status certo, como 405 (método) e 415 (formato do corpo)
        if (ex instanceof ErrorResponse erro && erro.getStatusCode().is4xxClientError()) {
            return ResponseEntity.status(erro.getStatusCode()).headers(erro.getHeaders()).body(Map.of(
                    "error", "Requisição inválida",
                    "message", "Esta rota não aceita a requisição enviada."
            ));
        }

        log.error("Erro inesperado", ex);
        var body = Map.of(
                "error", "Erro interno do servidor",
                "message", "Ocorreu um erro inesperado."
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    private record DadosErroValidacao(String campo, String mensagem){
        public DadosErroValidacao(FieldError erro){
            this(erro.getField(), erro.getDefaultMessage());
        }
    }
}
