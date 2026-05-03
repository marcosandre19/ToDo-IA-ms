package com.toDo.tarefas.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Item do array {@code errors} em respostas de erro de validação multi-campo
 * (escopo §6). Os nomes dos campos seguem o contrato JSON do escopo.
 */
public class ErroCampo {

    @Schema(description = "Nome do campo do payload que violou a regra", example = "titulo")
    private String field;

    @Schema(description = "Mensagem da regra violada", example = "não deve estar em branco")
    private String message;

    public ErroCampo() {
    }

    public ErroCampo(String field, String message) {
        this.field = field;
        this.message = message;
    }

    public String getField() {
        return field;
    }

    public String getMessage() {
        return message;
    }
}
