# Projeto: API de Gerenciamento de Tarefas (MVP)

## Stack
- Java 11
- Spring Boot 2.7.18 (web, data-jpa, validation, actuator)
- H2 em modo arquivo (persistência local em `./data/tarefas`)
- Maven (wrapper `./mvnw`)
- IntelliJ IDEA

## Estado atual
Projeto Spring Boot 2.7.18 funcional, sem código de domínio ainda.
Setup: Maven Wrapper (`./mvnw`), `TarefasApplication` em `com.toDo.tarefas`, H2 em modo arquivo (`./data/tarefas.mv.db`), `/health` exposto via `spring-boot-starter-actuator` (remapeado de `/actuator/health`).
Documentação OpenAPI 3 via `springdoc-openapi-ui` 1.7.0 — Swagger UI em `/swagger-ui.html`, JSON em `/v3/api-docs`.

## Convenções
- Pacote base: `com.toDo.tarefas`
- Camadas: `controller`, `service`, `repository`, `entity`, `dto`, `exception`
- DTOs separados de entidades (não expor entidade JPA no controller)
- Validações via Bean Validation (`jakarta.validation` ou `javax.validation` conforme versão do Spring Boot)
- Respostas de erro padronizadas (JSON com `timestamp`, `status`, `error`, `message`, `path`)

## Idioma do código

**Português brasileiro** para identificadores de domínio: nomes de classes, métodos, variáveis, campos, mensagens de erro, comentários e JavaDoc. Exemplos: `Tarefa`, `TarefaNaoEncontradaException`, `TarefaRepository`, `TarefaService`, `TarefaController`, `"Tarefa com ID 42 não encontrada"`.

**Inglês** para:
- Anotações de framework e palavras-chave Java (`@Entity`, `@Service`, `RuntimeException`, `findById`).
- **Nomes de pacotes Java** (`entity`, `exception`, `dto`, `controller`, `service`, `repository`) — alinhado ao escopo §7 e ao pacote `entity` já estabelecido.

**Exceção pontual — DTO de resposta de erro (`ErroResponse`)**: campos seguem o contrato JSON do escopo §6 em inglês (`timestamp`, `status`, `error`, `message`, `path`), para serialização direta sem `@JsonProperty`. Os **valores** desses campos (mensagens, descrições) permanecem em pt-BR.

**Testes**: nomes de método de teste em pt-BR **sem acentos** (ex.: `deveCriarTarefaComSucesso`) — Java aceita acentos mas geram ruído em logs e ferramentas. `@DisplayName` com acentos é bem-vindo para legibilidade nos relatórios.

## Modo de trabalho
Desenvolvimento incremental por fases. Cada fase deve ser revisada antes da próxima começar.
A especificação técnica completa está em `escopo-todo.md`.

## Comandos úteis
- Rodar: `./mvnw spring-boot:run`
- Testar: `./mvnw test`
- Build: `./mvnw clean package`