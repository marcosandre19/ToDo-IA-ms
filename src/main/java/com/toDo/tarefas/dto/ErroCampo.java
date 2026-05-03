package com.toDo.tarefas.dto;

/**
 * Item do array {@code errors} em respostas de erro de validação multi-campo
 * (escopo §6). Os nomes dos campos seguem o contrato JSON do escopo.
 */
public class ErroCampo {

    private String field;
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
