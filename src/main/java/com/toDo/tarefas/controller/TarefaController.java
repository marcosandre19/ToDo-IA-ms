package com.toDo.tarefas.controller;

import com.toDo.tarefas.dto.TarefaRequest;
import com.toDo.tarefas.dto.TarefaResponse;
import com.toDo.tarefas.entity.enums.Prioridade;
import com.toDo.tarefas.entity.enums.StatusTarefa;
import com.toDo.tarefas.service.TarefaService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.net.URI;
import java.util.List;

/**
 * Endpoints REST do recurso Tarefa, conforme escopo §4. O controller apenas
 * orquestra: aplica {@code @Valid} no payload, delega para o {@link TarefaService}
 * e devolve a resposta. Erros de domínio (id inexistente) e de validação são
 * tratados pelo {@code GlobalExceptionHandler}.
 */
@RestController
@RequestMapping("/api/tarefas")
@Validated
public class TarefaController {

    private final TarefaService tarefaService;

    public TarefaController(TarefaService tarefaService) {
        this.tarefaService = tarefaService;
    }

    /**
     * Cria uma nova tarefa. Retorna {@code 201 Created} com header {@code Location}
     * apontando para o recurso criado.
     */
    @PostMapping
    public ResponseEntity<TarefaResponse> criar(@Valid @RequestBody TarefaRequest request) {
        TarefaResponse resposta = tarefaService.criar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(resposta.getId())
                .toUri();
        return ResponseEntity.created(location).body(resposta);
    }

    /**
     * Lista tarefas, opcionalmente filtradas por {@code status} e/ou
     * {@code prioridade}. Resultado ordenado por {@code criadoEm} desc.
     */
    @GetMapping
    public ResponseEntity<List<TarefaResponse>> listar(
            @RequestParam(required = false) StatusTarefa status,
            @RequestParam(required = false) Prioridade prioridade) {
        return ResponseEntity.ok(tarefaService.listar(status, prioridade));
    }

    /**
     * Busca uma tarefa pelo id.
     */
    @GetMapping("/{id}")
    public ResponseEntity<TarefaResponse> buscarPorId(
            @PathVariable @Min(value = 1, message = "deve ser positivo") Long id) {
        return ResponseEntity.ok(tarefaService.buscarPorId(id));
    }

    /**
     * Atualiza uma tarefa existente (substituição completa, escopo §4.4).
     */
    @PutMapping("/{id}")
    public ResponseEntity<TarefaResponse> atualizar(
            @PathVariable @Min(value = 1, message = "deve ser positivo") Long id,
            @Valid @RequestBody TarefaRequest request) {
        return ResponseEntity.ok(tarefaService.atualizar(id, request));
    }

    /**
     * Remove uma tarefa pelo id.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(
            @PathVariable @Min(value = 1, message = "deve ser positivo") Long id) {
        tarefaService.remover(id);
        return ResponseEntity.noContent().build();
    }
}
