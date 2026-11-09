# OllMCP — cliente MCP para Ollama

Este diretório documenta o uso do `ollmcp`, um cliente de linha de comando que conecta um modelo Ollama a ferramentas disponibilizadas por um servidor MCP. Ele funciona como um utilitário de teste independente; o agente Java deste repositório também possui seu próprio cliente MCP.

## Como funciona

Um modelo de linguagem não se conecta diretamente a um endpoint MCP. O cliente MCP faz a conexão, obtém as ferramentas disponíveis, encaminha as solicitações do modelo e executa as ferramentas chamadas por ele.

O OllMCP aceita servidores MCP locais ou remotos via SSE ou Streamable HTTP. Neste laboratório, o servidor escolar usa SSE em `http://localhost:8081/mcp/sse`.

## Requisitos

- Python com `pip` para instalar o cliente
- [Ollama](https://ollama.com/) instalado e em execução
- O servidor [escola-poc-mcp-server](../escola-poc-mcp-server/README.md) ativo na porta `8081`
- O modelo de chat `llama3.2` disponível no Ollama

Instale o cliente e baixe o modelo:

```bash
python -m pip install ollmcp
ollama pull llama3.2
```

## Conectar ao servidor da escola

Com o servidor MCP e o Ollama em execução, use:

```bash
ollmcp --provider ollama --model llama3.2 \
  --mcp-server-url http://localhost:8081/mcp/sse
```

O comando abre a interface interativa do cliente. Para conferir as opções disponíveis na versão instalada:

```bash
ollmcp --help
```

## Modelo de embedding para o RAG

O modelo de embedding converte texto em vetores para busca semântica; ele é diferente do modelo de chat. O agente Java usa `nomic-embed-text` para indexar e consultar seus relatórios:

```bash
ollama pull nomic-embed-text
```

Também é possível iniciar o modelo e enviar um texto de teste diretamente ao Ollama:

```bash
ollama run nomic-embed-text "your text here"
```

Esse modelo é necessário para o RAG do agente Java, não para o uso básico do cliente `ollmcp`.

## Referência

Link de referência que constava nesta documentação: https://share.google/aimode/VGozJlbE3zKipmg8E
