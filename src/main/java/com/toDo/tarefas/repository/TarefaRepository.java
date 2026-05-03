package com.toDo.tarefas.repository;

import com.toDo.tarefas.entity.Tarefa;
import com.toDo.tarefas.entity.enums.Prioridade;
import com.toDo.tarefas.entity.enums.StatusTarefa;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TarefaRepository extends JpaRepository<Tarefa, Long> {

    List<Tarefa> findByStatus(StatusTarefa status, Sort sort);

    List<Tarefa> findByPrioridade(Prioridade prioridade, Sort sort);

    List<Tarefa> findByStatusAndPrioridade(StatusTarefa status, Prioridade prioridade, Sort sort);
}
