# Escopo Técnico — API de Gerenciamento de Tarefas (MVP)

## 1. Objetivo do MVP
Expor uma API REST em Spring Boot para CRUD de tarefas pessoais, com persistência local em H2 (modo arquivo), validação de entrada via Bean Validation e respostas de erro padronizadas. O MVP cobre apenas o recurso `Tarefas` (sem autenticação, sem múltiplos usuários, sem anexos).

## 2. Stack e dependências

| Item | Versão | Justificativa |
|---|---|---|
| Java | 11 | Requisito do projeto; LTS suportada pelo Spring Boot 2.7.x. |
| Spring Boot | 2.7.18 | Última 2.x compatível com Java 11 (Spring Boot 3.x exige Java 17). |
| `spring-boot-starter-web` | 2.7.18 | Servidor embarcado (Tomcat) e MVC para expor REST. |
| `spring-boot-starter-data-jpa` | 2.7.18 | Repositórios JPA sobre Hibernate, reduz boilerplate de DAO. |
| `spring-boot-starter-validation` | 2.7.18 | Bean Validation (`javax.validation`) para validar DTOs no controller. |
| `spring-boot-starter-actuator` | 2.7.18 | Endpoint `/health` com checagem automática de `DataSource` via `DataSourceHealthIndicator`. |
| `com.h2database:h2` | 2.1.x (gerenciada) | Banco embarcado em modo arquivo; zero infra para o MVP. |
| `spring-boot-starter-test` | 2.7.18 | JUnit 5, AssertJ, MockMvc para testes de unidade e integração. |
| Maven Wrapper (`mvnw`) | — | Build reprodutível sem exigir Maven instalado. |

> Spring Boot 2.7.x usa o namespace `javax.validation` (não `jakarta.validation`).

### Dívida técnica aceita: schema evolution via `ddl-auto=update`

O MVP usa `spring.jpa.hibernate.ddl-auto=update` em todos os profiles (`dev`, `prod`, testes usam `create-drop`). **Não há ferramenta de migration (Flyway/Liquibase) no escopo do MVP.**

Implicações conhecidas e aceitas:

- Hibernate adiciona colunas e tabelas novas, mas **não remove, não renomeia e não migra dados**.
- Renomear `titulo` → `title`, alterar tipo de `dataVencimento` ou remover um valor de enum exige intervenção manual no `./data/tarefas.mv.db` em cada ambiente.
- Estado do schema é **não-reproduzível**: dois desenvolvedores que rodaram versões diferentes do código sobre o mesmo arquivo H2 podem ter schemas divergentes sem perceber.
- O JAR do `RT-014` rodando num diretório limpo cria o schema do zero — comportamento diferente de uma máquina onde o schema evoluiu incrementalmente.

**Por que aceitar a dívida agora:**
- Projeto é um MVP de curso, sem usuários em produção e sem dados de valor.
- Adicionar Flyway no R1 dobraria o esforço de configuração inicial sem ganho proporcional.

**Quando pagar a dívida (pós-MVP):**

Disparam a migração para Flyway:
- Primeira mudança destrutiva de schema (rename, drop, mudança de tipo).
- Saída do H2 para um SGBD compartilhado (Postgres/MySQL).
- Entrada de qualquer dado real que não possa ser recriado.

**Plano de pagamento (não faz parte do MVP, registrado para referência):**
1. Adicionar `flyway-core` ao `pom.xml`.
2. Gerar `db/migration/V1__init.sql` a partir do schema atual (export do H2 ou via `schema-h2.sql` gerado pelo Hibernate).
3. Trocar `ddl-auto` para `validate` em todos os profiles.
4. Cada PR posterior que altera entidade JPA acompanha uma migration `V{N}__descricao.sql`.

### Configuração do H2 (`application.properties`)
```properties
spring.datasource.url=jdbc:h2:file:./data/tarefas;AUTO_SERVER=TRUE
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=update
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# Contrato temporal (§3)
spring.jackson.serialization.write-dates-as-timestamps=false
spring.jackson.time-zone=UTC

# Actuator — health remapeado para /health (ver §4.0)
management.endpoints.web.base-path=/
management.endpoints.web.path-mapping.health=health
management.endpoints.web.exposure.include=health
management.endpoint.health.show-details=when_authorized
```

## 3. Modelo de dados

Entidade JPA `Tarefas` mapeada para a tabela `tarefas`.

| Campo | Tipo Java | Coluna SQL | Restrições | Default |
|---|---|---|---|---|
| `id` | `Long` | `BIGINT` | PK, auto-incremento (`IDENTITY`) | — |
| `titulo` | `String` | `VARCHAR(120)` | NOT NULL, 1–120 chars | — |
| `descricao` | `String` | `VARCHAR(1000)` | NULLABLE, até 1000 chars | `NULL` |
| `status` | `StatusTarefa` (enum) | `VARCHAR(20)` | NOT NULL | `PENDENTE` |
| `prioridade` | `Prioridade` (enum) | `VARCHAR(10)` | NOT NULL | `MEDIA` |
| `dataVencimento` | `LocalDate` | `DATE` | NULLABLE; se presente, ≥ data atual na criação | `NULL` |
| `criadoEm` | `OffsetDateTime` | `TIMESTAMP WITH TIME ZONE` | NOT NULL, imutável | `OffsetDateTime.now(Clock UTC)` na criação |
| `atualizadoEm` | `OffsetDateTime` | `TIMESTAMP WITH TIME ZONE` | NOT NULL | `OffsetDateTime.now(Clock UTC)` na criação e a cada update |

### Contrato temporal

Decisões fixas para evitar ambiguidade entre dev local, CI e empacotamento:

- **Timezone canônico do serviço:** UTC. Todos os instantes (`criadoEm`, `atualizadoEm`) são gravados e serializados em UTC, com offset explícito `Z` no JSON.
- **Tipo Java para instantes:** `OffsetDateTime` (não `LocalDateTime`). Serializado pelo Jackson no formato ISO-8601 com offset, ex.: `"2026-05-03T14:22:31Z"`.
- **Tipo SQL:** `TIMESTAMP WITH TIME ZONE` no H2 (compatível com Postgres futuro, ver dívida técnica).
- **`dataVencimento`:** mantém-se `LocalDate` (data civil, sem zona). Formato JSON `"YYYY-MM-DD"`.
- **Regra "hoje ou futuro" do `dataVencimento`:** "hoje" é calculado em **America/Sao_Paulo** (`ZoneId.of("America/Sao_Paulo")`), não em UTC, para refletir a expectativa do usuário humano. Implementação deve usar `LocalDate.now(clock.withZone(...))` com `Clock` injetável.
- **Cliente do JSON:** consumidor da API deve assumir que todos os campos `*Em` chegam em UTC e converter para timezone local na apresentação.

### Enums
- `StatusTarefa`: `PENDENTE`, `EM_ANDAMENTO`, `CONCLUIDA`, `CANCELADA`
- `Prioridade`: `BAIXA`, `MEDIA`, `ALTA`

## 4. Endpoints da API

Base path do recurso de tarefas: `/api/tarefas`. O endpoint `/health` é exposto na raiz para simplificar verificações externas (load balancer, CI, monitoração).

| Método | Path | Descrição | Status esperados |
|---|---|---|---|
| `GET` | `/health` | Verificação de disponibilidade do serviço | `200 OK`, `503 Service Unavailable` |
| `POST` | `/api/tarefas` | Cria nova tarefa | `201 Created`, `400 Bad Request` |
| `GET` | `/api/tarefas` | Lista tarefas (filtros opcionais por `status`, `prioridade`) | `200 OK` |
| `GET` | `/api/tarefas/{id}` | Busca tarefa por id | `200 OK`, `404 Not Found` |
| `PUT` | `/api/tarefas/{id}` | Atualiza tarefa (substituição completa) | `200 OK`, `400 Bad Request`, `404 Not Found` |
| `PATCH` | `/api/tarefas/{id}/status` | Atualiza apenas o status | `200 OK`, `400 Bad Request`, `404 Not Found` |
| `DELETE` | `/api/tarefas/{id}` | Remove tarefa | `204 No Content`, `404 Not Found` |

### 4.0 `GET /health`

Endpoint de liveness/readiness usado para verificar se a aplicação está no ar e se a conexão com o H2 está saudável. Não exige autenticação e não é versionado sob `/api`.

Implementação: **`spring-boot-starter-actuator`** com `HealthEndpoint` remapeado de `/actuator/health` para `/health` (configuração em §2). O `DataSourceHealthIndicator` é registrado automaticamente e roda `Connection#isValid(timeout)` em cada chamada.

Política de detalhes: `management.endpoint.health.show-details=when_authorized`. Como o MVP não tem autenticação, o corpo nunca exibe os componentes individuais — apenas o agregado `{"status":"UP"}`. Em release futuro com auth, usuários autorizados verão o bloco `components`.

Response `200 OK` (serviço disponível):
```json
{ "status": "UP" }
```

Response `503 Service Unavailable` (dependência crítica indisponível, ex.: H2 inacessível):
```json
{ "status": "DOWN" }
```

Quando `show-details=always` (ex.: profile dev), o corpo inclui `components`:
```json
{
  "status": "UP",
  "components": {
    "db": { "status": "UP", "details": { "database": "H2", "validationQuery": "isValid()" } },
    "diskSpace": { "status": "UP" },
    "ping": { "status": "UP" }
  }
}
```

> Decisão arquitetural: usar o actuator (em vez de um `HealthController` custom) reduz código próprio, herda checagens automáticas (db, diskSpace, ping) e segue convenção idiomática do Spring Boot. O remap para `/health` mantém o path simples (sem prefixo `/actuator`) e não requer mudanças em load balancers ou consumidores.

### 4.1 `POST /api/tarefas`

Request:
```json
{
  "titulo": "Revisar pull request #42",
  "descricao": "Validar testes da camada de service e cobertura.",
  "prioridade": "ALTA",
  "dataVencimento": "2026-05-10"
}
```

Response `201 Created` (header `Location: /api/tarefas/1`):
```json
{
  "id": 1,
  "titulo": "Revisar pull request #42",
  "descricao": "Validar testes da camada de service e cobertura.",
  "status": "PENDENTE",
  "prioridade": "ALTA",
  "dataVencimento": "2026-05-10",
  "criadoEm": "2026-05-03T14:22:31Z",
  "atualizadoEm": "2026-05-03T14:22:31Z"
}
```

### 4.2 `GET /api/tarefas`

Request: `GET /api/tarefas?status=PENDENTE&prioridade=ALTA`

Response `200 OK`:
```json
[
  {
    "id": 1,
    "titulo": "Revisar pull request #42",
    "descricao": "Validar testes da camada de service e cobertura.",
    "status": "PENDENTE",
    "prioridade": "ALTA",
    "dataVencimento": "2026-05-10",
    "criadoEm": "2026-05-03T14:22:31",
    "atualizadoEm": "2026-05-03T14:22:31"
  }
]
```

### 4.3 `GET /api/tarefas/{id}`

Response `200 OK`:
```json
{
  "id": 1,
  "titulo": "Revisar pull request #42",
  "descricao": "Validar testes da camada de service e cobertura.",
  "status": "PENDENTE",
  "prioridade": "ALTA",
  "dataVencimento": "2026-05-10",
  "criadoEm": "2026-05-03T14:22:31Z",
  "atualizadoEm": "2026-05-03T14:22:31Z"
}
```

### 4.4 `PUT /api/tarefas/{id}`

Request:
```json
{
  "titulo": "Revisar pull request #42 (atualizado)",
  "descricao": "Conferir também o changelog.",
  "status": "EM_ANDAMENTO",
  "prioridade": "ALTA",
  "dataVencimento": "2026-05-12"
}
```

Response `200 OK`:
```json
{
  "id": 1,
  "titulo": "Revisar pull request #42 (atualizado)",
  "descricao": "Conferir também o changelog.",
  "status": "EM_ANDAMENTO",
  "prioridade": "ALTA",
  "dataVencimento": "2026-05-12",
  "criadoEm": "2026-05-03T14:22:31",
  "atualizadoEm": "2026-05-03T15:01:09Z"
}
```

### 4.5 `PATCH /api/tarefas/{id}/status`

Request:
```json
{ "status": "CONCLUIDA" }
```

Response `200 OK`:
```json
{
  "id": 1,
  "titulo": "Revisar pull request #42 (atualizado)",
  "descricao": "Conferir também o changelog.",
  "status": "CONCLUIDA",
  "prioridade": "ALTA",
  "dataVencimento": "2026-05-12",
  "criadoEm": "2026-05-03T14:22:31",
  "atualizadoEm": "2026-05-03T16:40:55Z"
}
```

### 4.6 `DELETE /api/tarefas/{id}`

Response `204 No Content` (corpo vazio).

## 5. Regras de negócio e validações

1. `titulo` é obrigatório, com tamanho entre 1 e 120 caracteres (após `trim`).
2. `descricao` é opcional; se enviada, máximo 1000 caracteres.
3. `status` aceita apenas valores do enum `StatusTarefa`. Default na criação: `PENDENTE`.
4. `prioridade` aceita apenas valores do enum `Prioridade`. Default na criação: `MEDIA`.
5. `dataVencimento` é opcional. Quando enviada na **criação**, não pode ser anterior à data atual em `America/Sao_Paulo` (ver §3 "Contrato temporal").
6. `criadoEm` é definido pelo servidor na criação (em UTC) e nunca alterado posteriormente.
7. `atualizadoEm` é definido pelo servidor na criação (em UTC) e atualizado a cada `PUT`/`PATCH`.
8. Transições de `status` são livres no MVP (qualquer valor para qualquer valor); nenhuma máquina de estados.
9. Uma tarefa com `status = CONCLUIDA` ainda pode ser editada e excluída (sem trava).
10. Filtros do `GET /api/tarefas`: `status` e `prioridade` são opcionais e combináveis (AND). Valores inválidos retornam `400`.
11. IDs em path são `Long` positivos; valores não numéricos retornam `400`.

## 6. Tratamento de erros

Todas as respostas de erro seguem o formato:

```json
{
  "timestamp": "2026-05-03T14:22:31Z",
  "status": 400,
  "error": "Bad Request",
  "message": "titulo: não pode ser vazio",
  "path": "/api/tarefas"
}
```

Para erros de validação com múltiplos campos, `message` agrega os erros separados por `; `, e um campo extra `errors` é incluído:

```json
{
  "timestamp": "2026-05-03T14:22:31Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Erro de validação",
  "path": "/api/tarefas",
  "errors": [
    { "field": "titulo", "message": "não pode ser vazio" },
    { "field": "dataVencimento", "message": "deve ser hoje ou no futuro" }
  ]
}
```

### Mapeamento exceção → HTTP

| Exceção | HTTP | Quando |
|---|---|---|
| `MethodArgumentNotValidException` | 400 | Falha de Bean Validation no `@RequestBody` |
| `ConstraintViolationException` | 400 | Falha de validação em parâmetros de query/path |
| `HttpMessageNotReadableException` | 400 | JSON malformado ou enum inválido |
| `MethodArgumentTypeMismatchException` | 400 | Tipo de path/query inválido (ex.: `id` não numérico) |
| `TarefaNaoEncontradaException` (custom) | 404 | Recurso inexistente em `GET/PUT/PATCH/DELETE /{id}` |
| `DataIntegrityViolationException` | 409 | Violação de constraint de banco |
| `Exception` (fallback) | 500 | Qualquer erro não mapeado |

Centralização: `@RestControllerAdvice` em `exception/GlobalExceptionHandler`.

## 7. Estrutura de pacotes

```
src/main/java/com/toDo/tarefas/
├── TarefasApplication.java
├── controller/
│   └── TarefaController.java
├── service/
│   └── TarefaService.java
├── repository/
│   └── TarefaRepository.java
├── entity/
│   ├── Tarefas.java
│   ├── StatusTarefa.java
│   └── Prioridade.java
├── dto/
│   ├── TarefaRequest.java
│   ├── TarefaResponse.java
│   ├── AtualizarStatusRequest.java
│   └── ErroResponse.java
└── exception/
    ├── TarefaNaoEncontradaException.java
    └── GlobalExceptionHandler.java

src/main/resources/
├── application.properties
└── data.sql              (opcional, seed para desenvolvimento)

src/test/java/com/toDo/tarefas/
├── controller/TarefaControllerTest.java
├── service/TarefaServiceTest.java
└── repository/TarefaRepositoryTest.java
```

## 8. Fases incrementais de implementação

### Fase 1 — Configuração base e persistência
- **Entregas:** `pom.xml` com starters (web, data-jpa, validation, actuator, h2, test); `application.properties` com H2 em modo arquivo, contrato temporal e remapeamento do `/health`; entidade `Tarefas` com enums `StatusTarefa` e `Prioridade`; `TarefaRepository` (`JpaRepository<Tarefas, Long>`); `TarefasApplication` rodando.
- **Pronto quando:** `./mvnw spring-boot:run` sobe sem erro, `GET /health` responde `200 {"status":"UP"}`, console H2 acessível em `/h2-console`, e a tabela `tarefas` é criada automaticamente em `./data/tarefas.mv.db`.

### Fase 2 — DTOs e camada de serviço
- **Entregas:** `TarefaRequest`, `TarefaResponse`, `AtualizarStatusRequest` com anotações de Bean Validation; `TarefaService` com métodos `criar`, `listar(filtros)`, `buscarPorId`, `atualizar`, `atualizarStatus`, `remover`; mapeamento entidade ↔ DTO (manual, sem MapStruct no MVP); `TarefaNaoEncontradaException`.
- **Pronto quando:** testes unitários do `TarefaService` cobrem casos felizes e erro de "não encontrado", usando `@DataJpaTest` ou mock do repositório.

### Fase 3 — Controller e endpoints CRUD
- **Entregas:** `TarefaController` expondo os 6 endpoints CRUD da seção 4; uso de `ResponseEntity` com status corretos e header `Location` no POST; suporte a query params `status` e `prioridade` no GET de listagem. O `GET /health` é entregue automaticamente pelo actuator desde a Fase 1 (não exige código próprio — ver §4.0).
- **Pronto quando:** todos os endpoints CRUD respondem corretamente via `curl`/Postman e testes com `MockMvc` validam status codes e payloads.

### Fase 4 — Tratamento global de erros
- **Entregas:** `ErroResponse` (DTO); `GlobalExceptionHandler` com `@RestControllerAdvice` mapeando todas as exceções da seção 6; formato unificado de resposta de erro, incluindo o campo `errors` para validação multi-campo.
- **Pronto quando:** payloads inválidos, IDs inexistentes e JSON malformado retornam o formato padronizado, validado por testes de integração.

### Fase 5 — Testes de integração e refinamento
- **Entregas:** testes end-to-end com `@SpringBootTest` + `MockMvc` cobrindo o ciclo completo (criar → listar → atualizar status → buscar → remover); seed opcional em `data.sql`; revisão de mensagens de erro em português; README curto de execução.
- **Pronto quando:** `./mvnw test` passa 100%, cobertura mínima dos fluxos da seção 4, e a aplicação é demonstrável fim-a-fim.
