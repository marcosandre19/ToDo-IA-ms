package com.toDo.tarefas.service;

import com.toDo.tarefas.dto.TarefaRequest;
import com.toDo.tarefas.dto.TarefaResponse;
import com.toDo.tarefas.entity.Tarefa;
import com.toDo.tarefas.entity.enums.Prioridade;
import com.toDo.tarefas.entity.enums.StatusTarefa;
import com.toDo.tarefas.exception.TarefaNaoEncontradaException;
import com.toDo.tarefas.repository.TarefaRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Operações de negócio do recurso Tarefa. A camada de controller nunca recebe
 * nem devolve a entidade JPA: o service mapeia {@link TarefaRequest} para a
 * entidade na entrada e {@link TarefaResponse} na saída (mapeamento manual,
 * conforme RF-009).
 */
@Service
@Transactional(readOnly = true)
public class TarefaService {

    private static final Sort ORDENACAO_DEFAULT = Sort.by(Sort.Direction.DESC, "criadoEm");

    private final TarefaRepository tarefaRepository;

    public TarefaService(TarefaRepository tarefaRepository) {
        this.tarefaRepository = tarefaRepository;
    }

    /**
     * Persiste uma nova tarefa a partir do payload de request. Campos opcionais
     * omitidos ({@code status}, {@code prioridade}) preservam os defaults
     * declarados na entidade ({@code PENDENTE}, {@code MEDIA}).
     */
    @Transactional
    public TarefaResponse criar(TarefaRequest request) {
        Tarefa nova = new Tarefa();
        nova.setTitulo(request.getTitulo());
        nova.setDescricao(request.getDescricao());
        if (request.getStatus() != null) {
            nova.setStatus(request.getStatus());
        }
        if (request.getPrioridade() != null) {
            nova.setPrioridade(request.getPrioridade());
        }
        nova.setDataVencimento(request.getDataVencimento());
        return toResponse(tarefaRepository.save(nova));
    }

    /**
     * Busca uma tarefa pelo id.
     *
     * @throws TarefaNaoEncontradaException se não houver tarefa com o id informado
     */
    public TarefaResponse buscarPorId(Long id) {
        return toResponse(buscarEntidade(id));
    }

    /**
     * Lista tarefas opcionalmente filtradas por {@code status} e/ou
     * {@code prioridade}. Quando ambos os filtros são {@code null}, retorna
     * todas as tarefas. Resultado sempre ordenado por {@code criadoEm} desc.
     */
    public List<TarefaResponse> listar(StatusTarefa status, Prioridade prioridade) {
        List<Tarefa> tarefas;
        if (status != null && prioridade != null) {
            tarefas = tarefaRepository.findByStatusAndPrioridade(status, prioridade, ORDENACAO_DEFAULT);
        } else if (status != null) {
            tarefas = tarefaRepository.findByStatus(status, ORDENACAO_DEFAULT);
        } else if (prioridade != null) {
            tarefas = tarefaRepository.findByPrioridade(prioridade, ORDENACAO_DEFAULT);
        } else {
            tarefas = tarefaRepository.findAll(ORDENACAO_DEFAULT);
        }
        return tarefas.stream().map(this::toResponse).collect(Collectors.toList());
    }

    /**
     * Atualiza uma tarefa existente (substituição completa dos campos de
     * domínio, conforme escopo §4.4). Preserva {@code id} e {@code criadoEm};
     * {@code atualizadoEm} é gerenciado pelo {@code @PreUpdate} da entidade.
     *
     * @throws TarefaNaoEncontradaException se não houver tarefa com o id informado
     */
    @Transactional
    public TarefaResponse atualizar(Long id, TarefaRequest request) {
        Tarefa existente = buscarEntidade(id);
        existente.setTitulo(request.getTitulo());
        existente.setDescricao(request.getDescricao());
        existente.setStatus(request.getStatus());
        existente.setPrioridade(request.getPrioridade());
        existente.setDataVencimento(request.getDataVencimento());
        // saveAndFlush força o flush imediato, disparando @PreUpdate antes do
        // mapeamento — sem isso, atualizadoEm no response viria com o valor
        // antigo (a transação só comitaria depois do toResponse).
        return toResponse(tarefaRepository.saveAndFlush(existente));
    }

    /**
     * Remove uma tarefa pelo id.
     *
     * @throws TarefaNaoEncontradaException se não houver tarefa com o id informado
     */
    @Transactional
    public void remover(Long id) {
        if (!tarefaRepository.existsById(id)) {
            throw new TarefaNaoEncontradaException(id);
        }
        tarefaRepository.deleteById(id);
    }

    private Tarefa buscarEntidade(Long id) {
        return tarefaRepository.findById(id)
                .orElseThrow(() -> new TarefaNaoEncontradaException(id));
    }

    private TarefaResponse toResponse(Tarefa tarefa) {
        return new TarefaResponse(
                tarefa.getId(),
                tarefa.getTitulo(),
                tarefa.getDescricao(),
                tarefa.getStatus(),
                tarefa.getPrioridade(),
                tarefa.getDataVencimento(),
                tarefa.getCriadoEm(),
                tarefa.getAtualizadoEm()
        );
    }
}
