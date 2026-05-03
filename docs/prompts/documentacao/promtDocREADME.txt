# Contexto
MVP de API REST em Spring Boot 2.7.x (Java 11) para gerenciar tarefas. Projeto desenvolvido de forma incremental com auxílio de IA (Claude Code), por etapas documentadas em arquivos `.md`. Documentos existentes:
- `escopo-todo.md` — escopo técnico (modelo de dados, endpoints, regras)
- `docs/backlog.md` — backlog por release com IDs RF/RT, marcados como concluídos
- `docs/diagramas.md` — arquitetura e fluxos em Mermaid
- `CLAUDE.md` — convenções (idioma pt-br no domínio, inglês em sufixos técnicos)

**Leia os quatro documentos antes de começar.** O `README.md` deve refletir fielmente o que está documentado e implementado — não inventar features, não prometer o que não existe.

Etapas de implementação concluídas:
1. Conversão para Spring Boot ✅
2. Enums + Entidades (`Tarefa`, `StatusTarefa`, `Prioridade`) ✅
3. Exceções + DTO de erro ✅
4. Repository ✅
5. Service ✅
6. Controller + DTOs + Handler global ✅
7. Testes unitários do service ✅
8. Testes do controller (`@WebMvcTest`) ✅

Etapa atual: **gerar `README.md` completo do projeto**.

# Objetivo
Gerar um único arquivo `README.md` na raiz do projeto, completo, profissional e objetivo. Substituirá qualquer `README.md` existente — é geração total, não edição incremental.

# Pré-execução obrigatória

Antes de escrever uma linha do README:
1. Listar a estrutura real do projeto (`tree -I 'target|.idea|data|.git'` ou equivalente)
2. Ler os endpoints reais do `TarefaController.java` (paths exatos, métodos HTTP, status codes)
3. Ler `application.properties` para confirmar porta, URL do H2, configurações expostas do Actuator
4. Ler `pom.xml` para confirmar versão exata do Spring Boot, do Java, e dependências realmente usadas
5. Ler `Tarefa.java` e enums para confirmar campos e valores reais

**Tudo no README deve refletir o código real, não suposições.** Se algo não bater entre documentos `.md` e código, **o código vence** — e sinalizar a divergência no chat.

# Estrutura obrigatória do README

Na ordem abaixo, sem seções extras decorativas, sem emojis em headings:

## 1. Título + descrição curta
- `# API de Gerenciamento de Tarefas`
- 2-3 linhas de descrição: o que faz, em uma frase técnica. Sem "este projeto tem como objetivo..." — direto ao ponto.

## 2. Stack
Lista compacta com versões reais lidas do `pom.xml`:
- Java 11
- Spring Boot (versão exata)
- Spring Data JPA, Spring Web, Spring Validation, Spring Boot Actuator
- H2 Database (modo arquivo)
- JUnit 5, Mockito, AssertJ
- Maven (com Maven Wrapper)

## 3. Arquitetura
- Texto curto (1 parágrafo) explicando a arquitetura em camadas: controller → service → repository → JPA → H2
- **Diagrama Mermaid** do fluxo geral (pode reaproveitar do `docs/diagramas.md`, mas simplificado se necessário). Se reaproveitar, manter consistência exata
- Estrutura de pacotes em bloco de código (`tree`-style), refletindo o projeto real

## 4. Pré-requisitos
Lista mínima:
- JDK 11
- Git
- (Opcional) IntelliJ IDEA ou VS Code

**Não exigir Maven instalado** — o projeto usa Maven Wrapper (`./mvnw`).

## 5. Instalação
Comandos exatos, copy-paste-ready, em blocos `bash`:
```bash
git clone <URL>
cd <pasta>
./mvnw clean install
```

Se houver passos específicos do projeto (criação de pasta `data/`, etc.), incluir.

## 6. Execução
- Comando para rodar: `./mvnw spring-boot:run`
- Indicar porta (lida do `application.properties`)
- URLs principais: API, console H2, actuator/health
- Bloco de exemplo para testar que está no ar:
```bash
  curl http://localhost:8080/actuator/health
```

## 7. Endpoints
Tabela com os endpoints **reais** (lidos do `TarefaController`):

| Método | Path | Descrição | Status sucesso | Erros possíveis |
|--------|------|-----------|----------------|-----------------|
| `POST` | `/tarefas` | Cria tarefa | 201 | 400 |
| `GET` | `/tarefas` | Lista tarefas | 200 | — |
| ... | ... | ... | ... | ... |

Após a tabela, **um exemplo de `curl` para cada endpoint**, em blocos `bash`, com payloads reais (não placeholders). Exemplo de criação com response esperado em bloco `json` separado.

## 8. Modelo de dados
Tabela dos campos da entidade `Tarefa`:

| Campo | Tipo | Restrições | Descrição |
|-------|------|------------|-----------|
| `id` | `Long` | gerado | Identificador |
| `titulo` | `String` | obrigatório, ≤... | Título da tarefa |
| ... | ... | ... | ... |

Listar valores possíveis dos enums (`StatusTarefa`, `Prioridade`).

## 9. Tratamento de erros
- Estrutura do `ErroResponse` em bloco JSON
- Tabela mapeando exceção → status HTTP

## 10. Testes
- Como rodar: `./mvnw test`
- O que está coberto: testes unitários do service (Mockito), testes do controller (`@WebMvcTest` com `@MockBean`)
- O que **não** está coberto ainda (testes do repository, integração ponta a ponta) — com honestidade
- Como rodar uma classe específica: `./mvnw test -Dtest=TarefaServiceTest`

## 11. Configuração
- Local do banco H2: arquivo em `./data/tasks.mv.db`
- Console H2: URL e credenciais default (`sa` / vazio)
- Variáveis ou perfis Spring se houver
- Como resetar o banco: deletar pasta `data/`

## 12. Uso de IA no desenvolvimento
Seção honesta e específica, **não genérica**:
- O projeto foi desenvolvido com auxílio do **Claude Code** (Anthropic) seguindo metodologia incremental documentada
- **Processo**: requisitos → backlog por releases → diagramas → implementação por camadas → testes
- **Artefatos do processo** (apontar pros docs gerados): `escopo-todo.md`, `docs/backlog.md`, `docs/diagramas.md`, `CLAUDE.md`
- **Decisões humanas**: definição de escopo e regras de negócio, escolhas arquiteturais (stack, persistência, padrões), revisão crítica de cada etapa antes da próxima, decisões de aceitar/rejeitar sugestões da IA
- **O que a IA fez**: geração de código a partir de prompts estruturados, sugestão de estrutura, revisão de consistência entre documentos
- Frase de fechamento sobre verificação humana: todo código foi revisado e validado manualmente; o histórico de commits reflete as etapas incrementais

Mais útil que copy-paste de "AI-generated disclaimer" — descreve metodologia.

## 13. Limitações conhecidas
Lista do que o MVP **não tem** (sendo honesto):
- Sem autenticação/autorização
- Sem paginação na listagem
- Sem versionamento de schema (Flyway/Liquibase) — usa `ddl-auto=update`
- Sem testes de integração ponta a ponta
- Sem deploy configurado
- Sem CI/CD
- H2 em modo arquivo: adequado para desenvolvimento, **não para produção**
- (Outras lacunas reais — verificar honestamente)

## 14. Próximos passos
Lista do que faria sentido se o projeto continuasse, em ordem de prioridade:
1. Migration de schema com Flyway ou Liquibase
2. Testes de repository (`@DataJpaTest`) e integração (`@SpringBootTest`)
3. Documentação OpenAPI/Swagger (`springdoc-openapi`)
4. Paginação e filtros na listagem
5. Migração para PostgreSQL em ambiente produtivo
6. Autenticação (Spring Security + JWT)
7. CI/CD (GitHub Actions)

Cada item em uma linha curta, sem subdivisão.

## 15. Licença (opcional)
Se houver decisão de licença, citar. Se não, omitir a seção (não inventar).

# Estilo
- Markdown limpo, headings hierárquicos consistentes (`#` para título, `##` para seções principais)
- **Sem emojis em headings** (parecem amadores em projeto técnico)
- Tabelas para dados tabulares (endpoints, modelo, mapeamento de erros)
- Blocos de código com linguagem especificada (` ```bash `, ` ```json `, ` ```java `, ` ```mermaid `)
- Comandos copy-paste-ready, testáveis
- Idioma: **português brasileiro** (consistente com o resto do projeto)
- **Sem badges falsos** (cobertura, build status) — só usar badges se forem reais
- **Sem seção "Contributing"** se não há diretriz definida — não inventar
- **Sem seção "Authors"** com email/perfil que não foi informado
- Tom: técnico, direto, sem entusiasmo de marketing ("amazing", "powerful", "robust")

# Critérios de "pronto"
- [ ] Todos os endpoints listados batem **exatamente** com o `TarefaController.java` (paths, métodos, status)
- [ ] Versão do Spring Boot na seção Stack bate com a do `pom.xml`
- [ ] Comando de execução (`./mvnw spring-boot:run`) funciona localmente
- [ ] Comandos `curl` de exemplo são executáveis e produzem o que está documentado
- [ ] Estrutura de pacotes reflete o projeto real
- [ ] Modelo de dados bate com `Tarefa.java` (campos, tipos, restrições)
- [ ] Limitações são honestas (não há feature listada que não existe; não há ausência maquiada)
- [ ] Seção de IA descreve **metodologia**, não disclaimer genérico
- [ ] Sem emojis em headings, sem badges falsos, sem tom de marketing
- [ ] Não há referência a arquivo, comando ou URL que não existe

# Restrições
- Substituir totalmente o `README.md` existente (geração completa, não merge incremental)
- NÃO criar outros arquivos
- NÃO alterar código de produção, testes, `escopo-todo.md`, `docs/`, `pom.xml`, `application.properties`
- NÃO inventar URLs, comandos, features, autores, licenças
- NÃO incluir seções de marketing ("Why this project", "Acknowledgments" sem informação real)
- Se algo no escopo contradiz este prompt, **escopo ganha** — sinalizar a divergência
- Se algo no código contradiz a documentação `.md`, **código ganha** — sinalizar a divergência

# Resposta esperada
1. Confirmação dos arquivos lidos na pré-execução (lista curta)
2. Lista de divergências encontradas entre documentação e código real (se houver), e como cada uma foi resolvida no README
3. Caminho do arquivo gerado (`README.md`)
4. Tamanho aproximado (linhas)
5. Sumário das seções do README (já fica como índice mental)