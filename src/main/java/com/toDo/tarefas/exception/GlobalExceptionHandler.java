package com.toDo.tarefas.exception;

import com.toDo.tarefas.dto.ErroCampo;
import com.toDo.tarefas.dto.ErroResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Tradução centralizada de exceções para {@link ErroResponse} conforme escopo §6.
 *
 * <p>Erros de cliente (4xx) são logados em nível {@code WARN}; erros inesperados
 * (5xx) em {@code ERROR} com stack trace completa. Mensagens internas de
 * exceções não mapeadas não são propagadas no corpo da resposta para evitar
 * vazamento de informação.</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(TarefaNaoEncontradaException.class)
    public ResponseEntity<ErroResponse> tratarTarefaNaoEncontrada(
            TarefaNaoEncontradaException ex,
            HttpServletRequest request) {
        logger.warn("Recurso não encontrado em {}: {}", request.getRequestURI(), ex.getMessage());
        ErroResponse corpo = new ErroResponse(
                OffsetDateTime.now(ZoneOffset.UTC),
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(corpo);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> tratarValidacao(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        List<ErroCampo> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new ErroCampo(fe.getField(), fe.getDefaultMessage()))
                .collect(Collectors.toList());
        logger.warn("Erro de validação em {}: {} campo(s) inválido(s)",
                request.getRequestURI(), errors.size());
        ErroResponse corpo = new ErroResponse(
                OffsetDateTime.now(ZoneOffset.UTC),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Erro de validação",
                request.getRequestURI(),
                errors
        );
        return ResponseEntity.badRequest().body(corpo);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResponse> tratarTipoInvalido(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {
        String mensagem = String.format("Valor inválido para o parâmetro '%s'", ex.getName());
        logger.warn("Tipo inválido em {}: parâmetro '{}', valor recebido '{}'",
                request.getRequestURI(), ex.getName(), ex.getValue());
        ErroResponse corpo = new ErroResponse(
                OffsetDateTime.now(ZoneOffset.UTC),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                mensagem,
                request.getRequestURI()
        );
        return ResponseEntity.badRequest().body(corpo);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponse> tratarJsonMalformado(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {
        logger.warn("Mensagem não legível em {}: {}", request.getRequestURI(), ex.getMessage());
        ErroResponse corpo = new ErroResponse(
                OffsetDateTime.now(ZoneOffset.UTC),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Requisição inválida: corpo JSON malformado ou valor de enum inválido",
                request.getRequestURI()
        );
        return ResponseEntity.badRequest().body(corpo);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> tratarFallback(
            Exception ex,
            HttpServletRequest request) {
        logger.error("Erro inesperado em {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        ErroResponse corpo = new ErroResponse(
                OffsetDateTime.now(ZoneOffset.UTC),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "Erro interno do servidor",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(corpo);
    }
}
