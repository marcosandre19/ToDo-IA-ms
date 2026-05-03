package com.toDo.tarefas.controller;

import com.toDo.tarefas.dto.ErroResponse;
import com.toDo.tarefas.dto.TarefaRequest;
import com.toDo.tarefas.dto.TarefaResponse;
import com.toDo.tarefas.entity.enums.Prioridade;
import com.toDo.tarefas.entity.enums.StatusTarefa;
import com.toDo.tarefas.service.TarefaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Tarefas", description = "Operações de gerenciamento de tarefas")
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
    @Operation(summary = "Cria uma nova tarefa")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tarefa criada"),
            @ApiResponse(responseCode = "400", description = "Payload inválido",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
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
    @Operation(summary = "Lista tarefas, com filtros opcionais por status e prioridade")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de tarefas"),
            @ApiResponse(responseCode = "400", description = "Filtro inválido",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    public ResponseEntity<List<TarefaResponse>> listar(
            @RequestParam(required = false) StatusTarefa status,
            @RequestParam(required = false) Prioridade prioridade) {
        return ResponseEntity.ok(tarefaService.listar(status, prioridade));
    }

    /**
     * Busca uma tarefa pelo id.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Busca uma tarefa por identificador")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tarefa encontrada"),
            @ApiResponse(responseCode = "400", description = "Identificador inválido",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "404", description = "Tarefa não encontrada",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    public ResponseEntity<TarefaResponse> buscarPorId(
            @PathVariable @Min(value = 1, message = "deve ser positivo") Long id) {
        return ResponseEntity.ok(tarefaService.buscarPorId(id));
    }

    /**
     * Atualiza uma tarefa existente (substituição completa, escopo §4.4).
     */
    @PutMapping("/{id}")
    @Operation(summary = "Atualiza uma tarefa existente (substituição completa)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tarefa atualizada"),
            @ApiResponse(responseCode = "400", description = "Payload ou identificador inválido",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "404", description = "Tarefa não encontrada",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    public ResponseEntity<TarefaResponse> atualizar(
            @PathVariable @Min(value = 1, message = "deve ser positivo") Long id,
            @Valid @RequestBody TarefaRequest request) {
        return ResponseEntity.ok(tarefaService.atualizar(id, request));
    }

    /**
     * Remove uma tarefa pelo id.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Remove uma tarefa")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Tarefa removida"),
            @ApiResponse(responseCode = "400", description = "Identificador inválido",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "404", description = "Tarefa não encontrada",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    public ResponseEntity<Void> remover(
            @PathVariable @Min(value = 1, message = "deve ser positivo") Long id) {
        tarefaService.remover(id);
        return ResponseEntity.noContent().build();
    }
}
