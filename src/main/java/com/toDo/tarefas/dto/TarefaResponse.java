package com.toDo.tarefas.dto;

import com.toDo.tarefas.entity.enums.Prioridade;
import com.toDo.tarefas.entity.enums.StatusTarefa;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * Payload de saída do recurso Tarefa, conforme exemplos do escopo §4.
 *
 * <p>{@code criadoEm} e {@code atualizadoEm} são serializados em UTC com
 * offset explícito (contrato temporal, escopo §3).</p>
 */
public class TarefaResponse {

    private Long id;
    private String titulo;
    private String descricao;
    private StatusTarefa status;
    private Prioridade prioridade;
    private LocalDate dataVencimento;
    private OffsetDateTime criadoEm;
    private OffsetDateTime atualizadoEm;

    public TarefaResponse() {
    }

    public TarefaResponse(Long id,
                          String titulo,
                          String descricao,
                          StatusTarefa status,
                          Prioridade prioridade,
                          LocalDate dataVencimento,
                          OffsetDateTime criadoEm,
                          OffsetDateTime atualizadoEm) {
        this.id = id;
        this.titulo = titulo;
        this.descricao = descricao;
        this.status = status;
        this.prioridade = prioridade;
        this.dataVencimento = dataVencimento;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    public Long getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public StatusTarefa getStatus() {
        return status;
    }

    public Prioridade getPrioridade() {
        return prioridade;
    }

    public LocalDate getDataVencimento() {
        return dataVencimento;
    }

    public OffsetDateTime getCriadoEm() {
        return criadoEm;
    }

    public OffsetDateTime getAtualizadoEm() {
        return atualizadoEm;
    }
}
