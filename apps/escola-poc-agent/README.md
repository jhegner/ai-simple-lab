# Agente da Escola

Aplicação de linha de comando que responde, em português, a perguntas sobre o desempenho dos alunos. O agente combina um modelo de chat local via Ollama, ferramentas de consulta fornecidas pelo servidor MCP da escola e busca RAG nos relatórios pedagógicos em texto.

## Requisitos

- Java 21
- Maven
- [Ollama](https://ollama.com/) em execução local
- O projeto [escola-poc-mcp-server](../escola-poc-mcp-server/README.md) iniciado na porta `8081`

Baixe os modelos usados pela aplicação:

```bash
ollama pull llama3.2
ollama pull nomic-embed-text
```

O modelo `llama3.2` gera as respostas; `nomic-embed-text` gera os embeddings usados para indexar e pesquisar os documentos do RAG.

## Executar

Na raiz do repositório, inicie primeiro o Ollama e o servidor MCP. Em seguida, execute o agente:

```bash
cd apps/escola-poc-agent
mvn spring-boot:run
```

O cliente MCP está configurado em [application.yml](src/main/resources/application.yml) para conectar a `http://localhost:8081/mcp/sse`. Se alterar host, porta ou endpoint do servidor, atualize essa configuração.

Na inicialização, o agente lê os arquivos `src/main/resources/notas/*.txt`, divide os textos em trechos e os indexa em um `SimpleVectorStore` em memória. Por isso, os embeddings são gerados novamente a cada inicialização; mantenha o Ollama acessível também durante essa etapa.

## Comandos da CLI

| Comando | Descrição |
| --- | --- |
| `perguntar` ou `p` | Envia uma pergunta ao agente |
| `historico` ou `h` | Exibe as perguntas e respostas da conversa atual |
| `nova-sessao` ou `ns` | Inicia outra conversa e limpa a memória anterior |
| `ajuda` ou `?` | Mostra exemplos e comandos disponíveis |
| `exit` | Encerra a aplicação |

Exemplos:

```text
p "Como está a Ana Silva?"
p "Quais alunos da turma A estão em risco?"
p "Liste todos com nota abaixo de 5"
h
ns
```

O histórico conversacional e o índice vetorial são mantidos em memória; eles não persistem após o encerramento do processo.

## Estrutura relevante

- `src/main/java/.../config/AgentConfig.java`: prompt do sistema, memória de conversa e registro das ferramentas MCP.
- `src/main/java/.../config/RagConfig.java`: armazenamento vetorial em memória e parâmetros de busca.
- `src/main/java/.../rag/RagIngestor.java`: leitura, divisão e indexação dos documentos.
- `src/main/resources/notas/`: relatórios pedagógicos usados pelo RAG.

## Testes

```bash
cd apps/escola-poc-agent
mvn test
```