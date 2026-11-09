#!/usr/bin/env bash
set -Eeuo pipefail

ROOT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"
MCP_DIR="$ROOT_DIR/apps/escola-poc-mcp-server"
AGENT_DIR="$ROOT_DIR/apps/escola-poc-agent"
BIN_DIR="$ROOT_DIR/bin"

if ! command -v mvn >/dev/null 2>&1; then
    echo "Erro: Maven não encontrado. Instale o Maven antes de executar este script." >&2
    exit 1
fi

if ! command -v gnome-terminal >/dev/null 2>&1; then
    echo "Erro: gnome-terminal não encontrado; ele é necessário para abrir os dois terminais." >&2
    exit 1
fi

echo "Empacotando servidor MCP..."
mvn -f "$MCP_DIR/pom.xml" clean package

echo "Empacotando agente..."
mvn -f "$AGENT_DIR/pom.xml" clean package

find_jar() {
    local target_dir="$1"
    local -a jars=()
    local file

    for file in "$target_dir"/*.jar; do
        [[ -f "$file" && "$file" != *.original ]] && jars+=("$file")
    done

    if [[ "${#jars[@]}" -ne 1 ]]; then
        echo "Erro: esperado exatamente um JAR executável em $target_dir, encontrados ${#jars[@]}." >&2
        exit 1
    fi

    printf '%s\n' "${jars[0]}"
}

MCP_JAR="$(find_jar "$MCP_DIR/target")"
AGENT_JAR="$(find_jar "$AGENT_DIR/target")"

mkdir -p "$BIN_DIR"
cp -- "$MCP_JAR" "$BIN_DIR/escola-poc-mcp-server.jar"
cp -- "$AGENT_JAR" "$BIN_DIR/escola-poc-agent.jar"

echo "JARs copiados para $BIN_DIR"

gnome-terminal \
    --title="Terminal 1 - MCP Server" \
    -- bash -lc 'cd "$1" || exit 1; echo "Iniciando MCP Server..."; java -jar "$2"; status=$?; echo "MCP Server encerrado (código $status)."; exec bash' \
    bash "$MCP_DIR" "$BIN_DIR/escola-poc-mcp-server.jar"

gnome-terminal \
    --title="Terminal 2 - Agent" \
    -- bash -lc '
        cd "$1" || exit 1
        echo "Aguardando MCP Server em localhost:8081..."
        ready=0
        for ((attempt = 0; attempt < 60; attempt++)); do
            if (echo > /dev/tcp/127.0.0.1/8081) 2>/dev/null; then
                ready=1
                break
            fi
            sleep 1
        done
        if [[ "$ready" -ne 1 ]]; then
            echo "MCP Server não ficou disponível em 60 segundos."
            exec bash
        fi
        echo "Iniciando Agent..."
        java -jar "$2"
        status=$?
        echo "Agent encerrado (código $status)."
        exec bash
    ' bash "$AGENT_DIR" "$BIN_DIR/escola-poc-agent.jar"
