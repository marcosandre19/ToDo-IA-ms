package com.toDo.tarefas.exception;

/**
 * Lançada quando uma tarefa referenciada por id não é encontrada
 * no repositório. Mapeada para HTTP 404 pelo handler global da camada
 * de controller (ver escopo §6).
 */
public class TarefaNaoEncontradaException extends RuntimeException {

    public TarefaNaoEncontradaException(String mensagem) {
        super(mensagem);
    }

    public TarefaNaoEncontradaException(Long id) {
        super(String.format("Tarefa com ID %d não encontrada", id));
    }
}
