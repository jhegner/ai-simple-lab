package br.jhegner.labs.config;

import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração do pipeline RAG.
 *
 * ══════════════════════════════════════════════════════════
 *  CONCEITO: o que é RAG e por que esses dois beans existem
 * ══════════════════════════════════════════════════════════
 *
 *  RAG (Retrieval-Augmented Generation) resolve o problema de que
 *  o LLM não conhece seus documentos privados. O pipeline tem duas fases:
 *
 *  FASE 1 — Ingestão (roda uma vez, no startup):
 *    Arquivos .txt → chunks de texto → embeddings → SimpleVectorStore
 *    Veja: RagIngestor.java
 *
 *  FASE 2 — Retrieval (a cada pergunta do usuário):
 *    Pergunta → embedding → busca no VectorStore → trechos relevantes
 *    → injetados no prompt → LLM responde com base nos documentos reais
 *    Quem faz isso automaticamente: QuestionAnswerAdvisor
 *
 * ══════════════════════════════════════════════════════════
 *  MUDANÇAS DE IMPORT — Spring AI 2.0.x vs 1.0.x
 * ══════════════════════════════════════════════════════════
 *
 *  QuestionAnswerAdvisor:
 *    ANTES: org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor
 *    AGORA: org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor
 *
 *  SimpleVectorStore:
 *    ANTES: SimpleVectorStore.builder(embeddingModel).build()  ← mesmo
 *    AGORA: igual, mas requer dependência spring-ai-vector-store-advisor no pom
 *
 *  QuestionAnswerAdvisor instanciação:
 *    ANTES: new QuestionAnswerAdvisor(vectorStore)
 *    AGORA: QuestionAnswerAdvisor.builder(vectorStore).build()
 */
@Configuration
public class RagConfig {

    /**
     * SimpleVectorStore: armazena embeddings em memória (HashMap interno).
     *
     * - EmbeddingModel: auto-configurado pelo spring-ai-starter-model-ollama
     *   usando o modelo definido em application.yml
     *   (spring.ai.ollama.embedding.options.model: nomic-embed-text)
     *
     * - Os documentos são adicionados pelo RagIngestor no startup.
     *
     * - Para produção: substitua por PGVectorStore ou QdrantVectorStore
     *   mantendo a mesma interface VectorStore — sem mudar nada nesta
     *   classe nem no QuestionAnswerAdvisor.
     */
    @Bean
    public SimpleVectorStore vectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore
                .builder(embeddingModel)
                .build();
    }

    /**
     * QuestionAnswerAdvisor: o componente que faz o retrieval automaticamente.
     *
     * Em cada chamada ao ChatClient ele:
     *   1. Pega a pergunta do usuário
     *   2. Gera o embedding da pergunta
     *   3. Busca os topK chunks mais similares no VectorStore
     *   4. Injeta esses trechos no prompt antes de chamar o LLM
     *
     * topK(4): retorna os 4 trechos mais relevantes.
     * similarityThreshold(0.5): descarta chunks com similaridade < 50%
     *   (evita ruído quando nenhum documento é realmente relevante).
     *
     * Este bean é registrado no ChatClient como defaultAdvisor
     * no AgentConfig.java.
     */
    @Bean
    public QuestionAnswerAdvisor questionAnswerAdvisor(SimpleVectorStore vectorStore) {
        return QuestionAnswerAdvisor
                .builder(vectorStore)
                .searchRequest(SearchRequest.builder()
                        .topK(4)
                        .similarityThreshold(0.5)
                        .build())
                .build();
    }
}