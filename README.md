# ai-simple-lab

Laboratorio simples e pratico sobre os principais conceitos de IA.

## problema

Implementar um modelo de IA que responda a perguntas sobre as notas, a situação e outros dados dos alunos de uma escola de forma conversacional. O modelo deverá usar informações presentes nos documentos da escola e outras fontes de dados da instituição.

## tópicos do lab

- INTERFACE
  - CLI conversacional
    - Spring Shell - histórico de contexto
- AGENTE IA / ChatClient / ToolCallbackProvider
  - Harness
    - System prompt
    - Restrições
    - Persona
- INFRAESTRUTURA COGNITIVA
  - Ollama + LLaMA3
  - MCP Server (Spring)
  - RAG Pipeline
- DADOS
  - SQLite
  - Arquivos de notas
  - Vector Store

## arquitetura

![arquitetura](docs/ARQUITETURA.gif)

## plano de implementacao

![implementacao](docs/IMPLEMENTACAO.jpg)
