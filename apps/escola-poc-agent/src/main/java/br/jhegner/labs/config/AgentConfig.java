package br.jhegner.labs.config;


import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração central do agente IA.
 *
 * Reúne os três pilares do agente:
 *   1. Memória conversacional  (MessageChatMemoryAdvisor)
 *   2. RAG                     (QuestionAnswerAdvisor)
 *   3. MCP Tools               (SyncMcpToolCallbackProvider)
 *
 * ══════════════════════════════════════════════════════════
 *  ORDEM DOS ADVISORS — importa
 * ══════════════════════════════════════════════════════════
 *
 *  Spring AI executa os advisors na ordem em que são registrados.
 *
 *  1. MessageChatMemoryAdvisor — primeiro: injeta o histórico da
 *     conversa no prompt antes de qualquer enriquecimento.
 *
 *  2. QuestionAnswerAdvisor — segundo: faz a busca vetorial e
 *     injeta os trechos dos documentos depois do histórico.
 *
 *  Isso garante que o LLM receba: [histórico] + [trechos RAG] + [pergunta].
 */
@Configuration
public class AgentConfig {

    @Bean
    public ChatClient chatClient(
            ChatClient.Builder builder,
            ChatMemory chatMemory,
            QuestionAnswerAdvisor questionAnswerAdvisor,
            SyncMcpToolCallbackProvider mcpToolCallbackProvider) {

        return builder
                // ── Harness: persona e restrições de resposta ──────────────
                // Este system prompt vai em TODAS as chamadas ao LLM.
                // É aqui que você define o "contrato de comportamento" do agente.
                .defaultSystem("""
                        Você é o assistente da Escola Labs, especializado em informações
                        sobre o desempenho acadêmico dos alunos.
                        
                        REGRAS OBRIGATÓRIAS — siga sempre, sem exceções:
                        1. Responda SOMENTE sobre alunos e assuntos da Escola Labs.
                        2. SEMPRE use as ferramentas disponíveis para buscar dados reais.
                        3. NUNCA invente, suponha ou estime dados de alunos.
                        4. Se não encontrar um aluno: diga "Não encontrei esse aluno no sistema."
                        5. Se a pergunta não for sobre alunos ou a escola, diga:
                           "Só posso ajudar com informações sobre alunos da Escola Labs."
                        6. Responda sempre em português, de forma clara e objetiva.
                        7. Ao listar alunos, use marcadores de lista com traço (-).
                        8. Ao mencionar notas, sempre inclua uma casa decimal (ex: 8.5).
                        """)

                .defaultAdvisors(

                        // ── 1. Memória conversacional
                        // Injeta histórico das últimas mensagens em cada chamada.
                        // conversationId passado por chamada no AlunoShell.
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),

                        // ── 2. RAG: Recuperação de trechos de relatórios pedagógicos
                        // Busca trechos dos relatórios pedagógicos (notas/*.txt)
                        // mais próximos semanticamente da pergunta do usuário
                        // e os injeta no prompt automaticamente.
                        questionAnswerAdvisor

                ).defaultTools(mcpToolCallbackProvider).build();
    }
}