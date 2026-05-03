# Projeto: API de Gerenciamento de Tarefas (MVP)

## Stack
- Java 11
- Spring Boot (web, data-jpa, validation)
- H2 em modo arquivo (persistência local em `./data/tarefas`)
- Maven
- IntelliJ IDEA

## Estado atual
Projeto recém-criado via Spring Initializr. Sem código de domínio ainda.

## Convenções
- Pacote base: `com.toDo.tarefas`
- Camadas: `controller`, `service`, `repository`, `entity`, `dto`, `exception`
- DTOs separados de entidades (não expor entidade JPA no controller)
- Validações via Bean Validation (`jakarta.validation` ou `javax.validation` conforme versão do Spring Boot)
- Respostas de erro padronizadas (JSON com `timestamp`, `status`, `error`, `message`, `path`)

## Modo de trabalho
Desenvolvimento incremental por fases. Cada fase deve ser revisada antes da próxima começar.
A especificação técnica completa está em `escopo-todo.md`.

## Comandos úteis
- Rodar: `./mvnw spring-boot:run`
- Testar: `./mvnw test`
- Build: `./mvnw clean package`