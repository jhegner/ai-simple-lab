package br.jhegner.labs.config;

import br.jhegner.labs.tools.AlunoTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpConfig {

    /**
     * Registra as tools do AlunoTools no MCP Server.
     * <p>
     * O MethodToolCallbackProvider:
     * 1. Escaneia todos os métodos @Tool no objeto recebido
     * 2. Gera o JSON schema de cada um (nome, description, parâmetros)
     * 3. Os registra no endpoint /mcp/sse para o agente descobrir
     * <p>
     * Para adicionar novas tools: crie outro @Component com @Tool
     * e adicione-o aqui como segundo parâmetro do .toolObjects().
     */
    @Bean
    public ToolCallbackProvider alunoToolProvider(final AlunoTools alunoTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(alunoTools)
                .build();
    }
}