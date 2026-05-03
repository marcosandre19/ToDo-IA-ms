package com.toDo.tarefas.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Corpo padronizado de respostas de erro da API, conforme escopo §6.
 *
 * <p>Os nomes dos campos seguem o contrato JSON do escopo (em inglês),
 * para serialização direta sem mapeamento. Mensagens de domínio gravadas
 * em {@code message} e {@code error} permanecem em português.</p>
 *
 * <p>{@code timestamp} é gravado em UTC com offset explícito, conforme
 * o contrato temporal do escopo §3.</p>
 *
 * <p>{@code errors} é opcional e aparece no JSON apenas em erros de
 * validação multi-campo (escopo §6). Quando {@code null}, é omitido
 * da serialização via {@link JsonInclude}.</p>
 */
public class ErroResponse {

    private OffsetDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<ErroCampo> errors;

    public ErroResponse() {
    }

    public ErroResponse(OffsetDateTime timestamp,
                        int status,
                        String error,
                        String message,
                        String path) {
        this(timestamp, status, error, message, path, null);
    }

    public ErroResponse(OffsetDateTime timestamp,
                        int status,
                        String error,
                        String message,
                        String path,
                        List<ErroCampo> errors) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.errors = errors;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }

    public List<ErroCampo> getErrors() {
        return errors;
    }
}
