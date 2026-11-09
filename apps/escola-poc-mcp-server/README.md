
# Servidor MCP da Escola

Servidor Spring Boot que disponibiliza consultas sobre alunos como ferramentas MCP. O agente de IA e outros clientes compatíveis podem descobrir e chamar essas ferramentas pelo endpoint SSE.

## Requisitos

- Java 21
- Maven (ou o Maven Wrapper incluído)
- SQLite 3, opcional, para consultar o banco manualmente

## Executar

Na raiz do repositório:

```bash
cd apps/escola-poc-mcp-server
./mvnw spring-boot:run
```

Ou, se o Maven estiver instalado:

```bash
mvn spring-boot:run
```

O servidor inicia na porta `8081`. O endpoint SSE configurado para clientes MCP é `http://localhost:8081/mcp/sse`.

## Ferramentas MCP

As ferramentas estão implementadas em `AlunoTools` e consultam a tabela `alunos`. Elas permitem:

- Buscar alunos por nome (busca parcial, sem distinção entre maiúsculas e minúsculas).
- Listar alunos de uma turma.
- Filtrar alunos por nota acima ou abaixo de um valor.
- Filtrar alunos por status.
- Consultar um resumo estatístico geral.

Os dados de exemplo incluem alunos das turmas A, B e C. O esquema e os registros iniciais estão em `src/main/resources/schema.sql` e `src/main/resources/data.sql`.

> **Atenção:** a inicialização SQL está configurada como `always`, e `data.sql` executa `DELETE FROM alunos` antes de inserir os exemplos. Assim, os registros do banco são recriados em cada inicialização do servidor; não use esse banco local para armazenar alterações que precisem persistir.

## Consultar o SQLite

O arquivo do banco é `escola.db`, criado no diretório de trabalho em que o servidor é iniciado. Para inspecioná-lo, instale o cliente SQLite, se necessário:

```bash
sqlite3 --version
sudo apt update && sudo apt install sqlite3
```

Com o servidor parado ou em execução, abra o banco a partir desta pasta:

```bash
cd apps/escola-poc-mcp-server
sqlite3 escola.db
```

No prompt do SQLite, consulte os dados:

```sql
SELECT COUNT(*) FROM alunos;
SELECT * FROM alunos;
```

Saia do prompt com `.quit`. Como o banco é relativo ao diretório de trabalho, executar o servidor a partir de outra pasta pode criar o arquivo `escola.db` em outro local.

## Testes

```bash
cd apps/escola-poc-mcp-server
./mvnw test
```