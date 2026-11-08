# validador

Para conectar um modelo executando localmente (usando Ollama) a um servidor MCP executando localmente sobre SSE (Server-Sent Events) endpoint, não é possivel conexao direta.

Eh necessario uma aplicacao client no meio, atuando como orquestrador para obter as ferramentas do endpoint SSE, e alimenta o modelo e executa as ferramentas quando o modelo solicita.

## ollmcp (CLI client)

CLI client que permite conectar diretamente a modelos Ollama a endpoints locais ou remotos HTTP/SSE MCP.

https://share.google/aimode/VGozJlbE3zKipmg8E

$ ollmcp --provider ollama --model llama3.2 --mcp-server-url http://localhost:8081/sse
