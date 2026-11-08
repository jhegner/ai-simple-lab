package br.jhegner.labs.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Ingestão de documentos no VectorStore.
 *
 * ══════════════════════════════════════════════════════════
 *  CONCEITO: ETL pipeline do RAG
 * ══════════════════════════════════════════════════════════
 *
 *  Extract  → lê os arquivos .txt de resources/notas/
 *  Transform → divide em chunks com TokenTextSplitter
 *             e adiciona metadados (turma, fonte)
 *  Load     → salva no SimpleVectorStore
 *             (que internamente gera o embedding de cada chunk)
 *
 *  Implementa ApplicationRunner: roda UMA VEZ após todos os beans
 *  do Spring estarem prontos, antes de qualquer pergunta do usuário.
 *
 * ══════════════════════════════════════════════════════════
 *  TokenTextSplitter: por que dividir em chunks?
 * ══════════════════════════════════════════════════════════
 *
 *  O LLM tem uma janela de contexto limitada. Se você jogar
 *  o documento inteiro no prompt, ele pode não caber — e custa tokens.
 *  O splitter divide o texto em pedaços de ~400 tokens com overlap
 *  (sobreposição) para não perder contexto nas bordas dos chunks.
 *
 *  Chunk size 400 tokens ≈ ~300 palavras ≈ ~2 parágrafos.
 *  O QuestionAnswerAdvisor retorna apenas os 4 chunks mais relevantes
 *  (topK=4), injetando ~1200 palavras de contexto no prompt.
 */
@Slf4j
@Component
public class RagIngestor implements ApplicationRunner {

    private final SimpleVectorStore vectorStore;

    // Injeta todos os arquivos do padrão classpath:notas/*.txt
    // O Spring resolve o wildcard e retorna um array de Resources
    @Value("classpath:notas/*.txt")
    private Resource[] arquivosDeNotas;

    public RagIngestor(SimpleVectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (arquivosDeNotas == null || arquivosDeNotas.length == 0) {
            log.warn("RAG: nenhum arquivo encontrado em classpath:notas/*.txt");
            return;
        }

        log.info("RAG: iniciando ingestão de {} arquivo(s)...", arquivosDeNotas.length);

        // ── Splitter de texto ──────────────────────────────────────────
        // TokenTextSplitter está em spring-ai-commons (transitivo via starter)
        // defaultChunkSize=400 tokens, overlap=50 tokens entre chunks
        var splitter = TokenTextSplitter.builder()
                .withChunkSize(400)
                .withMinChunkSizeChars(50)   // descarta chunks muito pequenos
                .withKeepSeparator(true)     // mantém quebras de parágrafo
                .build();

        List<Document> todosOsChunks = new ArrayList<>();

        for (Resource arquivo : arquivosDeNotas) {
            try {
                String nomeArquivo = arquivo.getFilename();     // "turma-a.txt"
                String turma = extrairTurma(nomeArquivo);       // "A"
                String conteudo = arquivo.getContentAsString(StandardCharsets.UTF_8);

                // ── 1. EXTRACT: cria um Document com o conteúdo bruto ──
                var docOriginal = new Document(conteudo, Map.of(
                        "source", nomeArquivo,
                        "turma",  turma,
                        "tipo",   "relatorio_pedagogico"
                ));

                // ── 2. TRANSFORM: divide em chunks ─────────────────────
                List<Document> chunks = splitter.apply(List.of(docOriginal));

                // Propaga os metadados do documento original para cada chunk
                // (o splitter cria novos Documents sem os metadados originais)
                chunks.forEach(chunk ->
                        chunk.getMetadata().putAll(docOriginal.getMetadata()));

                todosOsChunks.addAll(chunks);

                log.info("RAG: arquivo '{}' → {} chunk(s)", nomeArquivo, chunks.size());

            } catch (IOException e) {
                log.error("RAG: erro ao ler arquivo '{}': {}",
                        arquivo.getFilename(), e.getMessage());
            }
        }

        if (todosOsChunks.isEmpty()) {
            log.warn("RAG: nenhum chunk gerado. Verifique os arquivos em resources/notas/");
            return;
        }

        // ── 3. LOAD: adiciona ao VectorStore ───────────────────────────
        // vectorStore.add() faz internamente:
        //   a) chama EmbeddingModel.embed(chunk.getText()) para cada chunk
        //   b) armazena o vetor + texto + metadados no HashMap interno
        //
        // O modelo de embedding (nomic-embed-text) é chamado via Ollama
        // para cada chunk — pode levar alguns segundos no primeiro boot.
        vectorStore.add(todosOsChunks);

        log.info("RAG: ingestão concluída — {} chunk(s) indexado(s) no VectorStore",
                todosOsChunks.size());
    }

    /**
     * Extrai a letra da turma do nome do arquivo.
     * "turma-a.txt" → "A", "turma-b.txt" → "B"
     */
    private String extrairTurma(String nomeArquivo) {
        if (nomeArquivo == null) return "desconhecida";
        // remove extensão e prefixo "turma-"
        String base = nomeArquivo.replace(".txt", "").replace("turma-", "");
        return base.toUpperCase();
    }
}
