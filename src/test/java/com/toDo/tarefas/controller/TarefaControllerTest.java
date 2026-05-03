package com.toDo.tarefas.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.toDo.tarefas.dto.TarefaRequest;
import com.toDo.tarefas.dto.TarefaResponse;
import com.toDo.tarefas.entity.enums.Prioridade;
import com.toDo.tarefas.entity.enums.StatusTarefa;
import com.toDo.tarefas.exception.DadosInvalidosException;
import com.toDo.tarefas.exception.TarefaNaoEncontradaException;
import com.toDo.tarefas.service.TarefaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TarefaController.class)
class TarefaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TarefaService tarefaService;

    // ====== POST /api/tarefas ======

    @Test
    @DisplayName("Deve criar tarefa e retornar 201 com header Location")
    void deveCriarTarefaERetornar201ComLocation() throws Exception {
        // Arrange
        TarefaRequest req = tarefaRequestValido();
        TarefaResponse resp = respostaPersistida(1L, OffsetDateTime.parse("2026-05-03T10:00:00Z"));
        when(tarefaService.criar(any(TarefaRequest.class))).thenReturn(resp);

        // Act + Assert
        mockMvc.perform(post("/api/tarefas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/tarefas/1")))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.titulo").value("Tarefa de teste"))
                .andExpect(jsonPath("$.status").value("PENDENTE"));

        verify(tarefaService).criar(any(TarefaRequest.class));
    }

    @Test
    void deveRetornar400QuandoCriarComTituloVazio() throws Exception {
        // Arrange
        String json = "{\"titulo\":\"\",\"status\":\"PENDENTE\",\"prioridade\":\"MEDIA\"}";

        // Act + Assert
        mockMvc.perform(post("/api/tarefas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Erro de validação"))
                .andExpect(jsonPath("$.path").value("/api/tarefas"))
                .andExpect(jsonPath("$.errors[?(@.field=='titulo')]").exists());

        verify(tarefaService, never()).criar(any());
    }

    @Test
    void deveRetornar400QuandoCriarComTituloMuitoLongo() throws Exception {
        // Arrange
        TarefaRequest req = tarefaRequestValido();
        req.setTitulo(String.join("", Collections.nCopies(121, "X")));

        // Act + Assert
        mockMvc.perform(post("/api/tarefas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[?(@.field=='titulo')]").exists());

        verify(tarefaService, never()).criar(any());
    }

    @Test
    void deveRetornar400QuandoBodyForJsonInvalido() throws Exception {
        // Arrange
        String jsonMalformado = "{ titulo: }";

        // Act + Assert
        mockMvc.perform(post("/api/tarefas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMalformado))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("malformado")));

        verify(tarefaService, never()).criar(any());
    }

    @Test
    void deveRetornar400QuandoServiceLancarDadosInvalidos() throws Exception {
        // Arrange — simula a regra de dataVencimento no passado disparada pelo service
        TarefaRequest req = tarefaRequestValido();
        req.setDataVencimento(LocalDate.of(2020, 1, 1));
        when(tarefaService.criar(any(TarefaRequest.class)))
                .thenThrow(new DadosInvalidosException("dataVencimento", "deve ser hoje ou no futuro"));

        // Act + Assert
        mockMvc.perform(post("/api/tarefas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("dataVencimento"))
                .andExpect(jsonPath("$.errors[0].message").value("deve ser hoje ou no futuro"));
    }

    // ====== GET /api/tarefas ======

    @Test
    void deveListarTarefasERetornar200() throws Exception {
        // Arrange
        TarefaResponse t1 = respostaPersistida(1L, OffsetDateTime.parse("2026-05-03T10:00:00Z"));
        TarefaResponse t2 = respostaPersistida(2L, OffsetDateTime.parse("2026-05-03T11:00:00Z"));
        when(tarefaService.listar(null, null)).thenReturn(List.of(t1, t2));

        // Act + Assert
        mockMvc.perform(get("/api/tarefas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    void deveRetornarListaVaziaERetornar200() throws Exception {
        // Arrange
        when(tarefaService.listar(null, null)).thenReturn(Collections.emptyList());

        // Act + Assert
        mockMvc.perform(get("/api/tarefas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void deveRetornar400QuandoFiltroStatusForInvalido() throws Exception {
        // Act + Assert
        mockMvc.perform(get("/api/tarefas").param("status", "INVALIDO"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("status")));

        verify(tarefaService, never()).listar(any(), any());
    }

    // ====== GET /api/tarefas/{id} ======

    @Test
    void deveBuscarTarefaPorIdERetornar200() throws Exception {
        // Arrange
        TarefaResponse resp = respostaPersistida(42L, OffsetDateTime.parse("2026-05-03T10:00:00Z"));
        when(tarefaService.buscarPorId(42L)).thenReturn(resp);

        // Act + Assert
        mockMvc.perform(get("/api/tarefas/42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.titulo").value("Tarefa de teste"));
    }

    @Test
    void deveRetornar404QuandoBuscarTarefaInexistente() throws Exception {
        // Arrange
        when(tarefaService.buscarPorId(99L)).thenThrow(new TarefaNaoEncontradaException(99L));

        // Act + Assert
        mockMvc.perform(get("/api/tarefas/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value(containsString("99")))
                .andExpect(jsonPath("$.path").value("/api/tarefas/99"));
    }

    @Test
    void deveRetornar400QuandoIdForZero() throws Exception {
        // Act + Assert
        mockMvc.perform(get("/api/tarefas/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[?(@.field=='id')]").exists());

        verify(tarefaService, never()).buscarPorId(any());
    }

    // ====== PUT /api/tarefas/{id} ======

    @Test
    void deveAtualizarTarefaERetornar200() throws Exception {
        // Arrange
        TarefaRequest req = tarefaRequestValido();
        TarefaResponse resp = respostaPersistida(1L, OffsetDateTime.parse("2024-01-01T10:00:00Z"));
        when(tarefaService.atualizar(eq(1L), any(TarefaRequest.class))).thenReturn(resp);

        // Act + Assert
        mockMvc.perform(put("/api/tarefas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(tarefaService).atualizar(eq(1L), any(TarefaRequest.class));
    }

    @Test
    void deveRetornar404QuandoAtualizarTarefaInexistente() throws Exception {
        // Arrange
        TarefaRequest req = tarefaRequestValido();
        when(tarefaService.atualizar(eq(99L), any(TarefaRequest.class)))
                .thenThrow(new TarefaNaoEncontradaException(99L));

        // Act + Assert
        mockMvc.perform(put("/api/tarefas/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("99")));
    }

    @Test
    void deveRetornar400QuandoAtualizarComTituloVazio() throws Exception {
        // Arrange
        String json = "{\"titulo\":\"\",\"status\":\"PENDENTE\",\"prioridade\":\"MEDIA\"}";

        // Act + Assert
        mockMvc.perform(put("/api/tarefas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[?(@.field=='titulo')]").exists());

        verify(tarefaService, never()).atualizar(any(), any());
    }

    // ====== DELETE /api/tarefas/{id} ======

    @Test
    void deveRemoverTarefaERetornar204() throws Exception {
        // Arrange — service.remover é void; default Mockito é no-op

        // Act + Assert
        mockMvc.perform(delete("/api/tarefas/1"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(tarefaService).remover(1L);
    }

    @Test
    void deveRetornar404QuandoRemoverTarefaInexistente() throws Exception {
        // Arrange
        doThrow(new TarefaNaoEncontradaException(99L)).when(tarefaService).remover(99L);

        // Act + Assert
        mockMvc.perform(delete("/api/tarefas/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("99")));
    }

    // ====== fixtures ======

    private TarefaRequest tarefaRequestValido() {
        TarefaRequest req = new TarefaRequest();
        req.setTitulo("Tarefa de teste");
        req.setStatus(StatusTarefa.PENDENTE);
        req.setPrioridade(Prioridade.MEDIA);
        return req;
    }

    private TarefaResponse respostaPersistida(Long id, OffsetDateTime criadoEm) {
        return new TarefaResponse(
                id,
                "Tarefa de teste",
                "Descricao",
                StatusTarefa.PENDENTE,
                Prioridade.MEDIA,
                LocalDate.of(2026, 12, 31),
                criadoEm,
                criadoEm
        );
    }
}
