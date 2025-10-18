package br.com.fiap.skilltrack_backend_java.skilltrack.exceptions;

import org.apache.coyote.BadRequestException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

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
        String msg = ex.getMostSpecificCause().getMessage();
        String detalhe;

        if (msg.contains("EMAIL_UNIQUE")) {
            detalhe = "Já existe um usuário cadastrado com este e-mail.";
        } else if (msg.contains("PLACA_UNIQUE")) {
            detalhe = "Já existe uma moto cadastrada com esta placa.";
        } else if (msg.contains("CHASSI_UNIQUE")) {
            detalhe = "Já existe uma moto cadastrada com este chassi.";
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

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> tratarErroDeConversao(HttpMessageNotReadableException ex) {
        Throwable causa = ex.getCause();

        if (causa instanceof InvalidFormatException ife) {
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

    // 500 - Erro genérico inesperado
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> tratarErro500(Exception ex) {
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
