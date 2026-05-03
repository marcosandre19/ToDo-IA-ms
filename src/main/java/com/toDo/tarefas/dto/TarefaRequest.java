package com.toDo.tarefas.dto;

import com.toDo.tarefas.entity.enums.Prioridade;
import com.toDo.tarefas.entity.enums.StatusTarefa;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDate;

/**
 * Payload de entrada para criação ({@code POST /api/tarefas}) e substituição
 * completa ({@code PUT /api/tarefas/{id}}). {@code status} e {@code prioridade}
 * são opcionais na criação (defaults {@code PENDENTE}/{@code MEDIA} aplicados
 * pela entidade). No PUT, espera-se que o cliente envie todos os campos
 * conforme o escopo §4.4.
 */
public class TarefaRequest {

    @NotBlank
    @Size(min = 1, max = 120)
    private String titulo;

    @Size(max = 1000)
    private String descricao;

    private StatusTarefa status;

    private Prioridade prioridade;

    private LocalDate dataVencimento;

    public TarefaRequest() {
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public StatusTarefa getStatus() {
        return status;
    }

    public void setStatus(StatusTarefa status) {
        this.status = status;
    }

    public Prioridade getPrioridade() {
        return prioridade;
    }

    public void setPrioridade(Prioridade prioridade) {
        this.prioridade = prioridade;
    }

    public LocalDate getDataVencimento() {
        return dataVencimento;
    }

    public void setDataVencimento(LocalDate dataVencimento) {
        this.dataVencimento = dataVencimento;
    }
}
