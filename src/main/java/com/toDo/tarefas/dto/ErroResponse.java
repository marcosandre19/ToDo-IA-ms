package com.toDo.tarefas.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

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

    @Schema(description = "Instante do erro em UTC (ISO-8601 com offset)",
            example = "2026-05-03T14:22:31Z")
    private OffsetDateTime timestamp;

    @Schema(description = "Código HTTP", example = "400")
    private int status;

    @Schema(description = "Frase padrão do código HTTP", example = "Bad Request")
    private String error;

    @Schema(description = "Mensagem agregada do erro", example = "Erro de validação")
    private String message;

    @Schema(description = "Path da requisição que falhou", example = "/api/tarefas")
    private String path;

    @Schema(description = "Lista de erros por campo, presente apenas em validação multi-campo")
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
