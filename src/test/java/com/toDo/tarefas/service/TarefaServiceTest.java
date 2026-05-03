package com.toDo.tarefas.service;

import com.toDo.tarefas.dto.TarefaRequest;
import com.toDo.tarefas.dto.TarefaResponse;
import com.toDo.tarefas.entity.Tarefa;
import com.toDo.tarefas.entity.enums.Prioridade;
import com.toDo.tarefas.entity.enums.StatusTarefa;
import com.toDo.tarefas.exception.DadosInvalidosException;
import com.toDo.tarefas.exception.TarefaNaoEncontradaException;
import com.toDo.tarefas.repository.TarefaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TarefaServiceTest {

    @Mock
    private TarefaRepository tarefaRepository;

    @InjectMocks
    private TarefaService tarefaService;

    // ====== criar ======

    @Test
    @DisplayName("Deve criar tarefa com sucesso, retornando id e timestamps populados")
    void deveCriarTarefaComSucesso() {
        // Arrange
        TarefaRequest req = tarefaRequestValido();
        Tarefa salva = tarefaPersistida(1L, OffsetDateTime.parse("2026-05-03T10:00:00Z"));
        when(tarefaRepository.save(any(Tarefa.class))).thenReturn(salva);

        // Act
        TarefaResponse resposta = tarefaService.criar(req);

        // Assert
        assertThat(resposta.getId()).isEqualTo(1L);
        assertThat(resposta.getTitulo()).isEqualTo(salva.getTitulo());
        assertThat(resposta.getCriadoEm()).isEqualTo(OffsetDateTime.parse("2026-05-03T10:00:00Z"));
        verify(tarefaRepository).save(any(Tarefa.class));
    }

    @Test
    @DisplayName("Deve aplicar defaults PENDENTE e MEDIA quando status e prioridade forem omitidos")
    void deveAplicarDefaultsPendenteEMediaQuandoStatusEPrioridadeOmitidos() {
        // Arrange
        TarefaRequest req = new TarefaRequest();
        req.setTitulo("Sem defaults explicitos");
        ArgumentCaptor<Tarefa> captor = ArgumentCaptor.forClass(Tarefa.class);
        when(tarefaRepository.save(captor.capture()))
                .thenAnswer(inv -> inv.getArgument(0));

        // Act
        tarefaService.criar(req);

        // Assert
        Tarefa entidadePersistida = captor.getValue();
        assertThat(entidadePersistida.getStatus()).isEqualTo(StatusTarefa.PENDENTE);
        assertThat(entidadePersistida.getPrioridade()).isEqualTo(Prioridade.MEDIA);
    }

    @Test
    void deveLancarExcecaoQuandoCriarComDataVencimentoNoPassado() {
        // Arrange
        TarefaRequest req = tarefaRequestValido();
        req.setDataVencimento(LocalDate.of(2020, 1, 1));

        // Act + Assert
        assertThatThrownBy(() -> tarefaService.criar(req))
                .isInstanceOf(DadosInvalidosException.class)
                .hasMessageContaining("hoje ou no futuro");
        verify(tarefaRepository, never()).save(any());
    }

    // ====== listar ======

    @Test
    void deveRetornarListaVaziaQuandoNaoExistemTarefas() {
        // Arrange
        when(tarefaRepository.findAll(any(Sort.class))).thenReturn(Collections.emptyList());

        // Act
        List<TarefaResponse> resultado = tarefaService.listar(null, null);

        // Assert
        assertThat(resultado).isEmpty();
        verify(tarefaRepository).findAll(any(Sort.class));
    }

    @Test
    void deveListarTodasQuandoSemFiltros() {
        // Arrange
        Tarefa t1 = tarefaPersistida(1L, OffsetDateTime.parse("2026-05-03T10:00:00Z"));
        Tarefa t2 = tarefaPersistida(2L, OffsetDateTime.parse("2026-05-03T11:00:00Z"));
        when(tarefaRepository.findAll(any(Sort.class))).thenReturn(List.of(t1, t2));

        // Act
        List<TarefaResponse> resultado = tarefaService.listar(null, null);

        // Assert
        assertThat(resultado).hasSize(2);
        assertThat(resultado).extracting(TarefaResponse::getId).containsExactly(1L, 2L);
    }

    @Test
    void deveFiltrarPorStatusQuandoApenasStatusInformado() {
        // Arrange
        Tarefa t = tarefaPersistida(1L, OffsetDateTime.parse("2026-05-03T10:00:00Z"));
        when(tarefaRepository.findByStatus(eq(StatusTarefa.PENDENTE), any(Sort.class)))
                .thenReturn(List.of(t));

        // Act
        List<TarefaResponse> resultado = tarefaService.listar(StatusTarefa.PENDENTE, null);

        // Assert
        assertThat(resultado).hasSize(1);
        verify(tarefaRepository).findByStatus(eq(StatusTarefa.PENDENTE), any(Sort.class));
        verify(tarefaRepository, never()).findAll(any(Sort.class));
        verify(tarefaRepository, never()).findByPrioridade(any(), any(Sort.class));
        verify(tarefaRepository, never()).findByStatusAndPrioridade(any(), any(), any(Sort.class));
    }

    @Test
    void deveFiltrarPorPrioridadeQuandoApenasPrioridadeInformada() {
        // Arrange
        Tarefa t = tarefaPersistida(1L, OffsetDateTime.parse("2026-05-03T10:00:00Z"));
        when(tarefaRepository.findByPrioridade(eq(Prioridade.ALTA), any(Sort.class)))
                .thenReturn(List.of(t));

        // Act
        List<TarefaResponse> resultado = tarefaService.listar(null, Prioridade.ALTA);

        // Assert
        assertThat(resultado).hasSize(1);
        verify(tarefaRepository).findByPrioridade(eq(Prioridade.ALTA), any(Sort.class));
        verify(tarefaRepository, never()).findAll(any(Sort.class));
    }

    @Test
    void deveFiltrarPorStatusEPrioridadeQuandoAmbosInformados() {
        // Arrange
        Tarefa t = tarefaPersistida(1L, OffsetDateTime.parse("2026-05-03T10:00:00Z"));
        when(tarefaRepository.findByStatusAndPrioridade(
                eq(StatusTarefa.EM_ANDAMENTO), eq(Prioridade.ALTA), any(Sort.class)))
                .thenReturn(List.of(t));

        // Act
        List<TarefaResponse> resultado = tarefaService.listar(
                StatusTarefa.EM_ANDAMENTO, Prioridade.ALTA);

        // Assert
        assertThat(resultado).hasSize(1);
        verify(tarefaRepository).findByStatusAndPrioridade(
                eq(StatusTarefa.EM_ANDAMENTO), eq(Prioridade.ALTA), any(Sort.class));
    }

    // ====== buscarPorId ======

    @Test
    void deveBuscarTarefaPorIdComSucesso() {
        // Arrange
        Tarefa t = tarefaPersistida(42L, OffsetDateTime.parse("2026-05-03T10:00:00Z"));
        when(tarefaRepository.findById(42L)).thenReturn(Optional.of(t));

        // Act
        TarefaResponse resposta = tarefaService.buscarPorId(42L);

        // Assert
        assertThat(resposta.getId()).isEqualTo(42L);
        assertThat(resposta.getTitulo()).isEqualTo(t.getTitulo());
    }

    @Test
    void deveLancarExcecaoQuandoBuscarTarefaInexistente() {
        // Arrange
        when(tarefaRepository.findById(99L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> tarefaService.buscarPorId(99L))
                .isInstanceOf(TarefaNaoEncontradaException.class)
                .hasMessageContaining("99");
    }

    // ====== atualizar ======

    @Test
    @DisplayName("Deve preservar criadoEm ao atualizar tarefa")
    void devePreservarCriadoEmAoAtualizarTarefa() {
        // Arrange
        OffsetDateTime criadoEmOriginal = OffsetDateTime.parse("2024-01-01T10:00:00Z");
        Tarefa existente = tarefaPersistida(1L, criadoEmOriginal);
        existente.setTitulo("Titulo original");
        when(tarefaRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(tarefaRepository.saveAndFlush(any(Tarefa.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        TarefaRequest novosDados = tarefaRequestValido();
        novosDados.setTitulo("Titulo atualizado");

        // Act
        TarefaResponse resposta = tarefaService.atualizar(1L, novosDados);

        // Assert
        assertThat(resposta.getCriadoEm()).isEqualTo(criadoEmOriginal);
        assertThat(resposta.getTitulo()).isEqualTo("Titulo atualizado");
        verify(tarefaRepository).saveAndFlush(existente);
    }

    @Test
    void deveLancarExcecaoQuandoAtualizarStatusNulo() {
        // Arrange
        TarefaRequest req = tarefaRequestValido();
        req.setStatus(null);

        // Act + Assert
        assertThatThrownBy(() -> tarefaService.atualizar(1L, req))
                .isInstanceOf(DadosInvalidosException.class);
        verify(tarefaRepository, never()).saveAndFlush(any());
    }

    @Test
    void deveLancarExcecaoQuandoAtualizarPrioridadeNula() {
        // Arrange
        TarefaRequest req = tarefaRequestValido();
        req.setPrioridade(null);

        // Act + Assert
        assertThatThrownBy(() -> tarefaService.atualizar(1L, req))
                .isInstanceOf(DadosInvalidosException.class);
        verify(tarefaRepository, never()).saveAndFlush(any());
    }

    @Test
    void deveLancarExcecaoQuandoAtualizarTarefaInexistente() {
        // Arrange
        TarefaRequest req = tarefaRequestValido();
        when(tarefaRepository.findById(99L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> tarefaService.atualizar(99L, req))
                .isInstanceOf(TarefaNaoEncontradaException.class)
                .hasMessageContaining("99");
        verify(tarefaRepository, never()).saveAndFlush(any());
    }

    // ====== remover ======

    @Test
    void deveRemoverTarefaComSucesso() {
        // Arrange
        when(tarefaRepository.existsById(1L)).thenReturn(true);

        // Act
        tarefaService.remover(1L);

        // Assert
        verify(tarefaRepository).deleteById(1L);
    }

    @Test
    void deveLancarExcecaoQuandoRemoverTarefaInexistente() {
        // Arrange
        when(tarefaRepository.existsById(99L)).thenReturn(false);

        // Act + Assert
        assertThatThrownBy(() -> tarefaService.remover(99L))
                .isInstanceOf(TarefaNaoEncontradaException.class)
                .hasMessageContaining("99");
        verify(tarefaRepository, never()).deleteById(any());
    }

    // ====== fixtures ======

    private TarefaRequest tarefaRequestValido() {
        TarefaRequest req = new TarefaRequest();
        req.setTitulo("Tarefa de teste");
        req.setStatus(StatusTarefa.PENDENTE);
        req.setPrioridade(Prioridade.MEDIA);
        return req;
    }

    /**
     * Constrói uma {@link Tarefa} com campos privados (id, criadoEm, atualizadoEm)
     * preenchidos via reflection — necessário em testes unitários, já que esses
     * campos são populados em produção pelo banco e pelos callbacks JPA
     * ({@code @PrePersist}, {@code @PreUpdate}).
     */
    private Tarefa tarefaPersistida(Long id, OffsetDateTime criadoEm) {
        Tarefa t = new Tarefa();
        t.setTitulo("Tarefa persistida " + id);
        t.setStatus(StatusTarefa.PENDENTE);
        t.setPrioridade(Prioridade.MEDIA);
        ReflectionTestUtils.setField(t, "id", id);
        ReflectionTestUtils.setField(t, "criadoEm", criadoEm);
        ReflectionTestUtils.setField(t, "atualizadoEm", criadoEm);
        return t;
    }
}
