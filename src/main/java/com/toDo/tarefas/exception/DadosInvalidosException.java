package com.toDo.tarefas.exception;

/**
 * Sinaliza uma regra de negócio violada por um campo específico do payload —
 * tipicamente uma validação que não pôde ser expressa via Bean Validation
 * (ex.: regra dependente de timezone ou aplicável apenas em uma operação).
 *
 * <p>Mapeada para HTTP 400 pelo handler global, com o mesmo formato de resposta
 * de erros multi-campo (escopo §6).</p>
 */
public class DadosInvalidosException extends RuntimeException {

    private final String campo;

    public DadosInvalidosException(String campo, String mensagem) {
        super(mensagem);
        this.campo = campo;
    }

    public String getCampo() {
        return campo;
    }
}
