# Backlog — API de Gerenciamento de Tarefas (MVP)

Backlog derivado do escopo em `escopo-todo.md`, organizado em 3 releases incrementais. IDs `RF-XXX` (requisitos funcionais) e `RT-XXX` (requisitos técnicos) são sequenciais e independentes.

---

## Release 1 — Core

**Objetivo:** CRUD funcional ponta a ponta com persistência H2 e payloads válidos respondendo corretamente.

**Pronto quando:** os endpoints `POST`, `GET` (listar e por id), `PUT` e `DELETE` da seção 4 do escopo respondem com status e payloads esperados para entradas válidas, dados persistem entre reinícios, e `./mvnw spring-boot:run` sobe sem erro.

> Escopo do Core foi enxugado: o `PATCH /api/tarefas/{id}/status` (`RF-007`) e os filtros via query param do `GET /api/tarefas` (`RF-004.1`) foram movidos para o Release 2, pois dependem da validação de enums (`RF-012`) para responder `400` em valores inválidos. Sem essa validação, valores fora dos enums em query/body geram `500` indesejado.

### Requisitos técnicos

- [ ] **RT-001** — Configurar `pom.xml` com Spring Boot 2.7.18
  - [ ] `parent` declarado como `spring-boot-starter-parent:2.7.18`
  - [ ] Dependências presentes: `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-validation`, `spring-boot-starter-actuator`, `com.h2database:h2` (scope runtime), `spring-boot-starter-test` (scope test)
  - [ ] `java.version` = 11
  - [ ] `./mvnw clean compile` conclui sem erro

- [ ] **RT-002** — Criar classe principal `TarefasApplication` e estrutura de pacotes
  - [ ] Classe `com.toDo.tarefas.TarefasApplication` com `@SpringBootApplication` e `main`
  - [ ] Pacotes criados: `controller`, `service`, `repository`, `entity`, `dto`, `exception`
  - [ ] `./mvnw spring-boot:run` sobe a aplicação na porta 8080 sem erros no log

- [ ] **RT-003** — Configurar `application.properties` com H2 em modo arquivo
  - [ ] `spring.datasource.url=jdbc:h2:file:./data/tarefas;AUTO_SERVER=TRUE`
  - [ ] Driver, usuário (`sa`) e senha vazia configurados conforme escopo
  - [ ] `spring.jpa.hibernate.ddl-auto=update`
  - [ ] H2 console habilitado em `/h2-console`
  - [ ] Contrato temporal: `spring.jackson.serialization.write-dates-as-timestamps=false` e `spring.jackson.time-zone=UTC` (ver `escopo-todo.md` §3)
  - [ ] Actuator remapeado: `management.endpoints.web.base-path=/`, `management.endpoints.web.path-mapping.health=health`, `management.endpoints.web.exposure.include=health`, `management.endpoint.health.show-details=when_authorized`
  - [ ] Após `spring-boot:run`, arquivo `./data/tarefas.mv.db` é criado no filesystem

### Requisitos funcionais

- [x] **RF-001** — Modelar entidade `Tarefa` e enums
  - [x] Classe `entity.Tarefa` mapeada para tabela `tarefas` com todos os campos da seção 3 do escopo
  - [x] Enums `StatusTarefa` (`PENDENTE`, `EM_ANDAMENTO`, `CONCLUIDA`, `CANCELADA`) e `Prioridade` (`BAIXA`, `MEDIA`, `ALTA`) em `entity.enums`
  - [x] `id` com `@GeneratedValue(strategy = IDENTITY)`
  - [x] `criadoEm` e `atualizadoEm` populados automaticamente via `@PrePersist` / `@PreUpdate` (em UTC, `OffsetDateTime`)
  - [x] Tabela `tarefas` é criada no H2 com as colunas e tipos da seção 3

- [x] **RF-002** — Repositório JPA `TarefaRepository`
  - [x] Interface `repository.TarefaRepository extends JpaRepository<Tarefa, Long>`
  - [x] Suporte a filtro por `status` e `prioridade` via query methods derivados (`findByStatus`, `findByPrioridade`, `findByStatusAndPrioridade`)
  - [x] `save`, `findById`, `findAll`, `deleteById` operam sem erro contra o H2 (herdados de `JpaRepository`, sem redeclaração)

- [x] **RF-003** — Endpoint `POST /api/tarefas` (criação)
  - [x] Aceita JSON conforme exemplo 4.1 do escopo
  - [x] Retorna `201 Created` com header `Location: /api/tarefas/{id}`
  - [x] Corpo da resposta contém `id`, `criadoEm`, `atualizadoEm` populados pelo servidor
  - [x] `status` default = `PENDENTE` e `prioridade` default = `MEDIA` quando omitidos
  - [x] Registro persistido na tabela `tarefas`

- [x] **RF-004** — Endpoint `GET /api/tarefas` (listagem)
  - [x] Retorna `200 OK` com array de todas as tarefas
  - [x] Lista vazia retorna `200 OK` com `[]`
  - [x] Ordenação default por `criadoEm DESC`
  - [x] Suporte a query params (`status`, `prioridade`) entregue antecipadamente — ver `RF-004.1`

- [x] **RF-005** — Endpoint `GET /api/tarefas/{id}` (busca por id)
  - [x] Id existente: retorna `200 OK` com payload conforme exemplo 4.3
  - [x] Id inexistente: retorna `404 Not Found`

- [x] **RF-006** — Endpoint `PUT /api/tarefas/{id}` (substituição completa)
  - [x] Id existente: retorna `200 OK` com a tarefa atualizada
  - [x] Todos os campos enviados sobrescrevem os atuais
  - [x] `criadoEm` permanece inalterado, `atualizadoEm` é atualizado para o instante da requisição
  - [x] Id inexistente: retorna `404 Not Found`

- [x] **RF-008** — Endpoint `DELETE /api/tarefas/{id}` (remoção)
  - [x] Id existente: retorna `204 No Content` com corpo vazio e remove o registro
  - [x] Id inexistente: retorna `404 Not Found`

- [x] **RF-008.1** — Endpoint `GET /health` (verificação de disponibilidade via actuator)
  - [x] Dependência `spring-boot-starter-actuator` no `pom.xml`
  - [x] Endpoint exposto em `/health` (remapeado de `/actuator/health`) — fora do prefixo `/api`
  - [x] Aplicação saudável: retorna `200 OK` com `{"status":"UP"}` (corpo agregado, sem detalhes — `show-details=when_authorized` e MVP sem auth)
  - [x] Banco indisponível: `DataSourceHealthIndicator` muda agregado para `DOWN`, retorna `503 Service Unavailable` com `{"status":"DOWN"}`
  - [x] Validação da conexão é feita pelo `DataSourceHealthIndicator` automaticamente (Spring Boot herda o comportamento de `Connection#isValid(timeout)`)
  - [x] Endpoint não exige autenticação e não é coberto pela validação Bean Validation
  - [x] Outros endpoints do actuator (`/info`, `/metrics`, etc.) **não** são expostos no MVP (`exposure.include=health`)

- [x] **RF-009** — Camada de serviço `TarefaService` e DTOs
  - [x] `dto.TarefaRequest`, `dto.TarefaResponse`, `dto.AtualizarStatusRequest` criados (este último ainda sem método consumidor — `atualizarStatus` é release 2)
  - [x] `service.TarefaService` com métodos `criar`, `listar(filtros)`, `buscarPorId`, `atualizar`, `remover` (`atualizarStatus` adiado para release 2 junto com `RF-007`)
  - [x] Mapeamento entidade ↔ DTO manual via helpers `toResponse` e setters explícitos no `TarefaService` (sem MapStruct)
  - [x] Controller nunca expõe a entidade `Tarefa` diretamente — todos os endpoints recebem `TarefaRequest` e devolvem `TarefaResponse`

---

## Release 2 — Qualidade

**Objetivo:** validações de entrada, tratamento de erros padronizado, logs estruturados e cobertura de testes unitários e de integração.

**Pronto quando:** todas as regras da seção 5 do escopo são aplicadas, respostas de erro seguem o formato da seção 6, `./mvnw test` passa 100% e há logs nas operações de mutação.

### Requisitos funcionais

- [x] **RF-004.1** — `GET /api/tarefas` com filtros via query param (entregue antecipadamente junto com `RF-004`)
  - [x] Com `?status=PENDENTE`: retorna apenas tarefas com aquele status
  - [x] Com `?prioridade=ALTA`: retorna apenas tarefas com aquela prioridade
  - [x] Filtros combinados (`?status=PENDENTE&prioridade=ALTA`): aplica AND
  - [x] Valor inválido em `?status=` ou `?prioridade=`: retorna `400` via `MethodArgumentTypeMismatchException` no `GlobalExceptionHandler`
  - [x] Mantém ordenação default por `criadoEm DESC`

- [ ] **RF-007** — Endpoint `PATCH /api/tarefas/{id}/status` (atualização de status)
  - [ ] Aceita JSON `{ "status": "CONCLUIDA" }`
  - [ ] Id existente: retorna `200 OK` com a tarefa atualizada (apenas `status` e `atualizadoEm` mudam)
  - [ ] Transições livres aceitas (qualquer valor → qualquer valor)
  - [ ] Id inexistente: retorna `404 Not Found`
  - [ ] Body sem `status` ou com valor inválido: retorna `400` (depende de `RF-012`)

- [x] **RF-010** — Validação de `titulo`
  - [x] `POST` e `PUT` com `titulo` ausente, vazio ou só whitespace: retornam `400` (via `@NotBlank` + trim no setter do DTO)
  - [x] `titulo` com mais de 120 caracteres: retorna `400` (via `@Size(min=1, max=120)`)
  - [x] `titulo` válido (1–120 chars após trim): aceito normalmente; trim aplicado no setter de `TarefaRequest`

- [x] **RF-011** — Validação de `descricao`
  - [x] `descricao` ausente ou `null`: aceita (campo opcional)
  - [x] `descricao` com mais de 1000 caracteres: retorna `400` (via `@Size(max=1000)`)

- [ ] **RF-012** — Validação de enums `status` e `prioridade`
  - [ ] Valor fora dos enums em `POST`/`PUT`/`PATCH`: retorna `400` com mensagem indicando os valores aceitos
  - [ ] Valor inválido em query param `?status=` ou `?prioridade=`: retorna `400`

- [x] **RF-013** — Validação de `dataVencimento`
  - [x] "Hoje" é calculado em `America/Sao_Paulo` (ver `escopo-todo.md` §3 "Contrato temporal")
  - [x] `dataVencimento` no passado em `POST`: retorna `400` com mensagem `deve ser hoje ou no futuro`
  - [x] `dataVencimento` igual a hoje ou futura: aceita
  - [x] `dataVencimento` ausente ou `null`: aceita
  - [x] Em `PUT`, datas passadas são aceitas (regra aplica-se apenas à criação)

- [x] **RF-014** — Validação de `id` em path
  - [x] `id` não numérico (ex.: `/api/tarefas/abc`): retorna `400` (via `MethodArgumentTypeMismatchException`)
  - [x] `id` numérico inexistente: retorna `404`
  - [x] `id` zero ou negativo: retorna `400` via `@Min(1)` + `ConstraintViolationException`

- [ ] **RF-015** — Resposta de erro padronizada (single-field)
  - [ ] Toda resposta de erro contém `timestamp`, `status`, `error`, `message`, `path`
  - [ ] `timestamp` em formato ISO-8601 local
  - [ ] `path` reflete o endpoint chamado

- [ ] **RF-016** — Resposta de erro com múltiplos campos
  - [ ] Erro de validação com 2+ campos inclui campo `errors` (array de `{field, message}`)
  - [ ] `message` = `"Erro de validação"` quando há múltiplos erros
  - [ ] Cada item de `errors` referencia o campo do DTO e a mensagem da constraint

### Requisitos técnicos

- [ ] **RT-004** — `GlobalExceptionHandler` com `@RestControllerAdvice`
  - [x] Classe `exception.GlobalExceptionHandler` anotada com `@RestControllerAdvice`
  - [x] Handlers para `MethodArgumentNotValidException` → 400
  - [x] Handler para `ConstraintViolationException` → 400 (entregue antecipadamente para suportar `@Min(1)` em path vars do `RF-014`)
  - [x] Handler para `HttpMessageNotReadableException` → 400 (JSON malformado, enum inválido)
  - [x] Handler para `MethodArgumentTypeMismatchException` → 400
  - [x] Handler para `TarefaNaoEncontradaException` → 404
  - [ ] Handler para `DataIntegrityViolationException` → 409 (release 2)
  - [x] Handler fallback para `Exception` → 500
  - [x] DTO `dto.ErroResponse` usado em todas as respostas

- [ ] **RT-005** — Exceção customizada `TarefaNaoEncontradaException`
  - [x] Classe `exception.TarefaNaoEncontradaException extends RuntimeException`
  - [ ] Lançada pelo `TarefaService` em `buscarPorId`, `atualizar`, `atualizarStatus`, `remover` quando id não existe

- [ ] **RT-006** — Logs nas operações de mutação
  - [ ] `TarefaService` usa SLF4J (`LoggerFactory.getLogger(...)`)
  - [ ] Log `INFO` ao criar, atualizar (PUT/PATCH) e remover, contendo o `id` da tarefa
  - [ ] Log `WARN` no `GlobalExceptionHandler` para 4xx, `ERROR` para 5xx
  - [ ] Logs visíveis no console ao executar `spring-boot:run`

- [ ] **RT-007** — Testes unitários do `TarefaService`
  - [x] Arquivo `service/TarefaServiceTest.java` com JUnit 5 + Mockito
  - [x] Cobre: criar (3 cenários), listar (4 cenários: vazia, sem filtros, status, prioridade, ambos), buscarPorId (encontrado e não encontrado), atualizar (preservação de criadoEm, status nulo, prioridade nula, id inexistente), remover (sucesso e id inexistente). `atualizarStatus` adiado para release 2 com `RF-007`.
  - [x] Verifica que `TarefaNaoEncontradaException` é lançada para id inexistente em buscar/atualizar/remover
  - [x] Verifica também `DadosInvalidosException` para regras de validação imperativa do service (status/prioridade nulos no PUT, dataVencimento no passado na criação)
  - [x] `./mvnw test` executa esses testes com 100% de aprovação

- [ ] **RT-008** — Testes do repositório com `@DataJpaTest`
  - [ ] Arquivo `repository/TarefaRepositoryTest.java`
  - [ ] Testa `save`, `findById`, `findAll` e o filtro por `status`/`prioridade`
  - [ ] Usa H2 em memória (perfil de teste isolado)

- [x] **RT-009** — Testes de controller com `MockMvc`
  - [x] Arquivo `controller/TarefaControllerTest.java` com **`@WebMvcTest(TarefaController.class)` + `@MockBean TarefaService`** (slice de web puro — não sobe contexto Spring inteiro nem usa banco real, isolamento por construção)
  - [x] Cobre o ciclo: criar → listar → buscar → atualizar → remover (atualizar status fica para release 2 com `RF-007`)
  - [x] Valida status codes, header `Location` no POST e estrutura de resposta de erro padronizada (`timestamp`, `status`, `error`, `message`, `path`, `errors[]`)
  - [x] Pelo menos 1 teste para cada status code (201, 200, 204, 400 multi-camada, 404)

- [ ] **RT-009.1** — Teste de fumaça do `/health`
  - [ ] Arquivo `HealthEndpointTest.java` com `@SpringBootTest` + `@AutoConfigureMockMvc`
  - [ ] Cenário UP: `GET /health` retorna `200 OK` com corpo `{"status":"UP"}` (apenas o agregado — `show-details=when_authorized`)
  - [ ] Não testar cenário DOWN: o `DataSourceHealthIndicator` é código do framework, já testado pelo Spring Boot. Reescrever esse cenário aqui adicionaria pouco valor.

---

## Release 3 — Entrega final

**Objetivo:** documentação consumível da API, instruções de execução, separação por profiles e ajustes de empacotamento para entrega.

**Pronto quando:** Swagger UI disponível, README permite a um terceiro rodar o projeto sem ajuda, profiles `dev` e `prod` funcionam isoladamente, e `./mvnw clean package` gera um JAR executável.

### Requisitos técnicos

- [x] **RT-010** — Integração OpenAPI/Swagger
  - [x] Dependência `springdoc-openapi-ui:1.7.0` adicionada ao `pom.xml`
  - [x] Swagger UI acessível em `/swagger-ui.html` com a aplicação rodando
  - [x] Spec OpenAPI JSON disponível em `/v3/api-docs`
  - [x] Os 5 endpoints CRUD documentados com método, path, parâmetros e schemas. `GET /health` é actuator e fica fora da documentação (`show-actuator=false`). `PATCH /api/tarefas/{id}/status` documentado quando `RF-007` for entregue na release 2.

- [x] **RT-011** — Anotações de documentação nos controllers e DTOs
  - [x] `TarefaController` anotado com `@Tag(name = "Tarefas", description = "Operações de gerenciamento de tarefas")`
  - [x] Cada endpoint anotado com `@Operation(summary = ...)` e `@ApiResponses` listando os status codes (4xx referencia `ErroResponse.class`)
  - [x] DTOs (`TarefaRequest`, `TarefaResponse`, `AtualizarStatusRequest`, `ErroResponse`, `ErroCampo`) com `@Schema(description, example)` em todos os campos
  - [x] Exemplos batem com escopo §4 (titulo "Revisar pull request #42", dataVencimento "2026-12-31", criadoEm com offset `Z` etc.)
  - [x] Bean `OpenApiConfig` em `config/` define title, description e version no cabeçalho da UI

- [ ] **RT-012** — Separação em profiles `dev` e `prod`
  - [ ] `application.properties` contém apenas configs comuns
  - [ ] `application-dev.properties`: H2 console habilitado, log nível `DEBUG` para `com.toDo.tarefas`, `ddl-auto=update`
  - [ ] `application-prod.properties`: H2 console desabilitado, log nível `INFO`, `ddl-auto=update` (mesma estratégia do dev — ver dívida técnica em `escopo-todo.md` §2)
  - [ ] `spring.profiles.active=dev` por padrão
  - [ ] `./mvnw spring-boot:run -Dspring-boot.run.profiles=prod` sobe no perfil prod sem erro

- [ ] **RT-013** — README de uso na raiz do projeto
  - [ ] Arquivo `README.md` na raiz com seções: Visão geral, Stack, Pré-requisitos, Como rodar, Como testar, Endpoints (link para Swagger), Health check, Estrutura de pastas
  - [ ] Comandos copiáveis: `./mvnw spring-boot:run`, `./mvnw test`, `./mvnw clean package`
  - [ ] Pelo menos 1 exemplo de `curl` para `POST /api/tarefas` e 1 para `GET /health`
  - [ ] Link para `docs/escopo-todo.md` e `docs/backlog.md`

- [ ] **RT-014** — Empacotamento JAR executável
  - [ ] `./mvnw clean package` gera JAR em `target/`
  - [ ] `java -jar target/<artefato>.jar` sobe a aplicação sem precisar de Maven
  - [ ] Arquivo `data/tarefas.mv.db` é criado no diretório de execução do JAR

- [ ] **RT-015** — Ajustes finais e revisão
  - [ ] Mensagens de erro revisadas e padronizadas em português
  - [ ] Remoção de logs `System.out` ou código morto
  - [ ] `./mvnw test` continua 100% verde
  - [ ] Demo manual: ciclo completo (criar → listar → atualizar → concluir → deletar) executado via Swagger UI sem erros

---

## Pendências de escopo

Lacunas ou ambiguidades identificadas no `escopo-todo.md` que precisam ser decididas antes ou durante a implementação:

1. **Paginação e ordenação no `GET /api/tarefas`** — escopo não define `page`, `size` ou `sort`. Listagem retornará todas as tarefas em uma única resposta. Definir se isso é aceitável para o MVP ou se deve ser adicionado.
2. **Validação de `dataVencimento` no `PUT`** — escopo restringe a regra "≥ hoje" apenas à criação (regra 5 da seção 5). Confirmar que `PUT` realmente aceita datas passadas.
3. **Comportamento do filtro com valor inválido** — regra 10 diz que valores inválidos retornam `400`, mas isso depende de o parâmetro chegar tipado (enum) ou string. Decidir se o binding de query param será via enum (gera 400 automático) ou string (validação manual).
4. **Profiles `dev` e `prod` não estão no escopo** — incluídos no Release 3 por boa prática de entrega; se não forem desejados, remover RT-012.
5. **README não consta na seção 7 do escopo** — `escopo-todo.md` menciona README curto apenas na Fase 5; Release 3 trata como entregável formal (RT-013).
6. **Política de CORS** — escopo é silente. Se um frontend for consumir a API em outro host/porta, será necessário configurar `@CrossOrigin` ou `WebMvcConfigurer`. Não incluído em nenhuma release.
7. **Auditoria/soft delete** — escopo define `DELETE` como remoção física. Confirmar que não há requisito de soft delete.

---

## Dívida técnica registrada

Itens conscientemente fora do MVP, com plano de pagamento documentado:

- **DT-001** — **Schema evolution sem ferramenta de migration.** MVP usa `ddl-auto=update` em todos os profiles. Detalhes, gatilhos para pagar a dívida e plano de migração para Flyway estão em `escopo-todo.md` §2 ("Dívida técnica aceita: schema evolution").
