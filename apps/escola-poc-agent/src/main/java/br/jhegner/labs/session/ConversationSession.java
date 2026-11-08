package br.jhegner.labs.session;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Mantém o estado da conversa atual.
 *
 * MUDANÇA Spring AI 2.0.x:
 * O ChatMemory (MessageWindowChatMemory) é auto-configurado pelo starter
 * e injetado diretamente no AgentConfig. Este bean cuida apenas de:
 *   - gerar e trocar o conversationId entre sessões
 *   - guardar o histórico de display para o comando :historico
 *
 * A limpeza do histórico no ChatMemory é feita via chatMemory.clear(id)
 * no AgentConfig quando o usuário pede nova sessão.
 */
@Component
public class ConversationSession {

    // ID único desta sessão — cada novaConversa() gera um novo UUID
    private String conversationId;

    // Pares [pergunta, resposta] para exibição no comando :historico
    private final List<String[]> displayHistory = new ArrayList<>();

    public ConversationSession() {
        this.conversationId = UUID.randomUUID().toString();
    }

    /**
     * Gera um novo conversationId e limpa o histórico de display.
     * O ChatMemory em si é limpo no AgentConfig via chatMemory.clear(antigoId).
     */
    public String novaConversa() {
        String antigoId = this.conversationId;
        this.conversationId = UUID.randomUUID().toString();
        this.displayHistory.clear();
        return antigoId;   // retorna o ID antigo para o caller limpar o ChatMemory
    }

    public String getConversationId() {
        return conversationId;
    }

    public void registrar(String pergunta, String resposta) {
        displayHistory.add(new String[]{pergunta, resposta});
    }

    public List<String[]> getDisplayHistory() {
        return Collections.unmodifiableList(displayHistory);
    }

    public int getTotalPerguntas() {
        return displayHistory.size();
    }
}
