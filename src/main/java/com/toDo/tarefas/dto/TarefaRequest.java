package com.toDo.tarefas.dto;

import com.toDo.tarefas.entity.enums.Prioridade;
import com.toDo.tarefas.entity.enums.StatusTarefa;
import io.swagger.v3.oas.annotations.media.Schema;

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

    @Schema(description = "Título da tarefa", example = "Revisar pull request #42")
    @NotBlank
    @Size(min = 1, max = 120)
    private String titulo;

    @Schema(description = "Descrição livre da tarefa",
            example = "Validar testes da camada de service e cobertura.")
    @Size(max = 1000)
    private String descricao;

    @Schema(description = "Status da tarefa. Default na criação: PENDENTE", example = "PENDENTE")
    private StatusTarefa status;

    @Schema(description = "Prioridade da tarefa. Default na criação: MEDIA", example = "ALTA")
    private Prioridade prioridade;

    @Schema(description = "Data de vencimento (opcional). Na criação, deve ser hoje ou futuro em America/Sao_Paulo",
            example = "2026-12-31")
    private LocalDate dataVencimento;

    public TarefaRequest() {
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo == null ? null : titulo.trim();
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao == null ? null : descricao.trim();
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
