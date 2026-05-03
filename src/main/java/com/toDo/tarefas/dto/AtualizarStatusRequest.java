package com.toDo.tarefas.dto;

import com.toDo.tarefas.entity.enums.StatusTarefa;

import javax.validation.constraints.NotNull;

/**
 * Payload de entrada para {@code PATCH /api/tarefas/{id}/status} (escopo §4.5).
 * Consumido pelo método {@code atualizarStatus} do service na release 2 (RF-007).
 */
public class AtualizarStatusRequest {

    @NotNull
    private StatusTarefa status;

    public AtualizarStatusRequest() {
    }

    public StatusTarefa getStatus() {
        return status;
    }

    public void setStatus(StatusTarefa status) {
        this.status = status;
    }
}
