package com.toDo.tarefas.dto;

import com.toDo.tarefas.entity.enums.Prioridade;
import com.toDo.tarefas.entity.enums.StatusTarefa;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * Payload de saída do recurso Tarefa, conforme exemplos do escopo §4.
 *
 * <p>{@code criadoEm} e {@code atualizadoEm} são serializados em UTC com
 * offset explícito (contrato temporal, escopo §3).</p>
 */
public class TarefaResponse {

    @Schema(description = "Identificador da tarefa", example = "1")
    private Long id;

    @Schema(description = "Título da tarefa", example = "Revisar pull request #42")
    private String titulo;

    @Schema(description = "Descrição livre da tarefa",
            example = "Validar testes da camada de service e cobertura.")
    private String descricao;

    @Schema(description = "Status atual da tarefa", example = "PENDENTE")
    private StatusTarefa status;

    @Schema(description = "Prioridade da tarefa", example = "ALTA")
    private Prioridade prioridade;

    @Schema(description = "Data de vencimento", example = "2026-12-31")
    private LocalDate dataVencimento;

    @Schema(description = "Instante de criação em UTC (ISO-8601 com offset)",
            example = "2026-05-03T14:22:31Z")
    private OffsetDateTime criadoEm;

    @Schema(description = "Instante da última atualização em UTC (ISO-8601 com offset)",
            example = "2026-05-03T14:22:31Z")
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
