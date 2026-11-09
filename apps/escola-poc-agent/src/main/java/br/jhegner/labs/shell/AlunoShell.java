package br.jhegner.labs.shell;

import br.jhegner.labs.session.ConversationSession;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

/**
 * Interface CLI do agente — comandos disponíveis no terminal.
 *
 * MUDANÇA Spring AI 2.0.x:
 * O conversationId é passado via ChatMemory.CONVERSATION_ID (constante nova).
 * Antes: MessageChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY
 * Agora: ChatMemory.CONVERSATION_ID
 */
@ShellComponent
@ShellCommandGroup("Assistente escolar")
public class AlunoShell {

    // Cores ANSI
    private static final String RESET  = "\u001B[0m";
    private static final String BOLD   = "\u001B[1m";
    private static final String DIM    = "\u001B[2m";
    private static final String CYAN   = "\u001B[36m";
    private static final String GREEN  = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";

    private final ChatClient chatClient;
    private final ConversationSession session;
    private final ChatMemory chatMemory;   // para limpar ao trocar sessão

    public AlunoShell(ChatClient chatClient,
                      ConversationSession session,
                      ChatMemory chatMemory) {
        this.chatClient  = chatClient;
        this.session     = session;
        this.chatMemory  = chatMemory;
    }

    // ══════════════════════════════════════════════════════
    //  perguntar / p  — comando principal
    // ══════════════════════════════════════════════════════

    @ShellMethod(
            key   = {"perguntar", "p"},
            value = "Faça uma pergunta sobre os alunos da escola"
    )
    public String perguntar(
            @ShellOption(
                    defaultValue = ShellOption.NULL,
                    help = "Sua pergunta entre aspas: p \"Quem tirou menos que 5?\""
            ) String pergunta) {

        if (pergunta == null || pergunta.isBlank()) {
            return colorir(YELLOW, "Use: p \"Sua pergunta aqui\"");
        }

        System.out.print(colorir(DIM, "  consultando..."));
        System.out.flush();

        String resposta;
        try {
            resposta = chatClient.prompt()
                    .user(pergunta)
                    // ── Passa o conversationId desta sessão ───────────────
                    // Isso faz o MessageChatMemoryAdvisor buscar/salvar o
                    // histórico correto para esta conversa específica.
                    //
                    // API correta no Spring AI 2.0.x:
                    //   ChatMemory.CONVERSATION_ID  (constante na interface)
                    //
                    // API antiga (1.0.x) que causava o erro de import:
                    //   MessageChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY
                    .advisors(a -> a.param(
                            ChatMemory.CONVERSATION_ID,
                            session.getConversationId()))
                    .call()
                    .content();

        } catch (Exception e) {
            System.out.print("\r                       \r");
            return colorir(YELLOW,
                    "Erro ao consultar o agente: " + e.getMessage()
                            + "\n  Verifique: ollama serve | mcp-server rodando na porta 8081");
        }

        System.out.print("\r                       \r");
        session.registrar(pergunta, resposta);

        return "\n" + colorir(GREEN, "Assistente: ") + resposta + "\n";
    }

    // ══════════════════════════════════════════════════════
    //  nova-sessao / ns
    // ══════════════════════════════════════════════════════

    @ShellMethod(
            key   = {"nova-sessao", "ns"},
            value = "Inicia nova conversa, apagando o histórico de contexto"
    )
    public String novaSessao() {
        int total = session.getTotalPerguntas();
        // novaConversa() retorna o ID antigo para limparmos o ChatMemory
        String antigoId = session.novaConversa();
        chatMemory.clear(antigoId);   // remove do repositório em memória
        return colorir(GREEN,
                "Nova sessão iniciada. %d pergunta(s) anterior(es) apagada(s).".formatted(total));
    }

    // ══════════════════════════════════════════════════════
    //  historico / h
    // ══════════════════════════════════════════════════════

    @ShellMethod(
            key   = {"historico", "h"},
            value = "Exibe as perguntas e respostas da sessão atual"
    )
    public String historico() {
        var hist = session.getDisplayHistory();
        if (hist.isEmpty()) {
            return colorir(DIM, "Nenhuma pergunta feita nesta sessão ainda.");
        }

        var sb = new StringBuilder();
        sb.append(colorir(BOLD + CYAN,
                "\n── Histórico (%d pergunta(s)) ──\n".formatted(hist.size())));

        for (int i = 0; i < hist.size(); i++) {
            String[] par = hist.get(i);
            sb.append("\n")
                    .append(colorir(CYAN, "[%d] Você: ".formatted(i + 1)))
                    .append(par[0]).append("\n")
                    .append(colorir(GREEN, "    Assistente: "))
                    .append(par[1].replace("\n", "\n    ")).append("\n");
        }

        sb.append(colorir(DIM, "\n────────────────────────────────\n"));
        return sb.toString();
    }

    // ══════════════════════════════════════════════════════
    //  ajuda / ?
    // ══════════════════════════════════════════════════════

    @ShellMethod(
            key   = {"ajuda", "?"},
            value = "Exibe exemplos de perguntas e comandos disponíveis"
    )
    public String ajuda() {
        return """

            %s%s── Assistente da Escola Beta ──%s

            %sExemplos de perguntas:%s
              p "Como está a Ana Silva?"
              p "Quais alunos da turma A estão em risco?"
              p "Liste todos com nota abaixo de 5"
              p "Me dê um panorama geral da escola"
              p "Quantos alunos foram reprovados?"

            %sComandos:%s
              perguntar  (p)    — fazer uma pergunta ao agente
              nova-sessao (ns)  — reiniciar o contexto da conversa
              historico   (h)   — ver perguntas e respostas anteriores
              ajuda       (?)   — esta tela
              exit              — sair

            """.formatted(
                BOLD, CYAN, RESET,
                BOLD, RESET,
                BOLD, RESET
        );
    }

    // ── Utilitário ────────────────────────────────────────

    private String colorir(String codigo, String texto) {
        return codigo + texto + RESET;
    }
}
