package com.coderpwh.advanced;

import co.elastic.clients.util.BinaryData;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.memory.InMemoryMemory;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.rag.Knowledge;
import io.agentscope.core.rag.RAGMode;
import io.agentscope.core.rag.knowledge.SimpleKnowledge;
import io.agentscope.core.rag.model.Document;
import io.agentscope.core.rag.reader.ReaderInput;
import io.agentscope.core.rag.reader.SplitStrategy;
import io.agentscope.core.rag.reader.TextReader;
import io.agentscope.core.rag.store.ElasticsearchStore;
import io.agentscope.core.tool.Toolkit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.agentscope.core.embedding.EmbeddingModel;
import io.agentscope.core.embedding.dashscope.DashScopeTextEmbedding;
import io.agentscope.core.formatter.dashscope.DashScopeChatFormatter;

import java.io.IOException;
import java.util.List;

public class ElasticsearchRAGExample {
    private static final Logger log = LoggerFactory.getLogger(ElasticsearchRAGExample.class);

    // Configuration
    private static final int EMBEDDING_DIMENSIONS = 1024;

    // Elasticsearch Configuration (Modify these as per your setup)
    private static final String ES_URL = System.getProperty("es.url", "http://localhost:9200");
    private static final String ES_USERNAME = System.getProperty("es.user", "elastic");
    private static final String ES_PASSWORD = System.getProperty("es.pass", "changeme");
    private static final String ES_INDEX_NAME = "agentscope_rag_example";


    public static void main(String[] args) {
        ExampleUtils.printWelcome(
                "Elasticsearch RAG 示例",
                "本示例演示基于 Elasticsearch 的 RAG 能力：\n"
                        + "  - 连接到 Elasticsearch 向量存储\n"
                        + "  - 使用稠密向量索引文档\n"
                        + "  - 由 ES 支持的智能知识检索");


        String apikey = ExampleUtils.getDashScopeApiKey();

        System.out.println("开始创建 Embeding 模型了");


        EmbeddingModel embeddingModel = DashScopeTextEmbedding.builder()
                .apiKey(apikey)
                .modelName("text-embedding-v3")
                .dimensions(EMBEDDING_DIMENSIONS)
                .build();

        System.out.println("创建完成");

        System.out.println("进行 es 数据库的连接");

        try {
            ElasticsearchStore elasticsearchStore = ElasticsearchStore.builder()
                    .url(ES_URL)
                    .username(ES_USERNAME)
                    .password(ES_PASSWORD)
                    .indexName(ES_INDEX_NAME)
                    .dimensions(EMBEDDING_DIMENSIONS)
                    .build();

            System.out.println("es 连接进行初始化等");

            Knowledge knowledge = SimpleKnowledge.builder()
                    .embeddingModel(embeddingModel)
                    .embeddingStore(elasticsearchStore)
                    .build();

            System.out.println("Knowledge 创建完成");

            System.out.println("开始创建es文档了");
            addSampleDocuments(knowledge);
            System.out.println("es 文档添加完成");
            System.out.println("es, 索引的名称是:" + ES_INDEX_NAME);

            System.out.println("开始进行 Agent RAG Model");
            demonstrateAgenticMode(apikey, knowledge);
        } catch (Exception e) {
            log.error("异常信息为:{}",e.getMessage());

        }


    }


    /***
     * 添加文档
     * @param knowledge
     */
    private static void addSampleDocuments(Knowledge knowledge) {
        // Sample documents about AgentScope and Elasticsearch
        String[] documents = {
                "AgentScope is a multi-agent system framework developed by ModelScope. It provides a"
                        + " unified interface for building and managing multi-agent applications.",
                "Elasticsearch is a distributed, RESTful search and analytics engine capable of "
                        + "performing vector similarity search using kNN (k-nearest neighbors).",
                "This specific example demonstrates how to replace the InMemoryStore with an "
                        + "ElasticsearchStore in AgentScope to persist knowledge data.",
                "RAG (Retrieval-Augmented Generation) combines LLMs with external knowledge retrieval "
                        + "to reduce hallucinations and provide up-to-date information.",
                "AgentScope allows developers to easily switch between different vector store"
                        + " implementations via the VDBStoreBase interface."
        };

        // Create reader for text documents
        TextReader reader = new TextReader(512, SplitStrategy.PARAGRAPH, 50);

        // Add each document
        for (int i = 0; i < documents.length; i++) {
            String docText = documents[i];
            ReaderInput input = ReaderInput.fromString(docText);

            try {
                List<Document> docs = reader.read(input).block();
                if (docs != null && !docs.isEmpty()) {
                    // This will embed the document and push it to Elasticsearch
                    knowledge.addDocuments(docs).block();
                    System.out.println(
                            "  Indexed document "
                                    + (i + 1)
                                    + ": "
                                    + docText.substring(0, Math.min(50, docText.length()))
                                    + "...");
                }
            } catch (Exception e) {
                System.err.println("  Error adding document " + (i + 1) + ": " + e.getMessage());
            }
        }
    }

    private static void demonstrateAgenticMode(String apiKey, Knowledge knowledge) throws IOException {

        ReActAgent agent = ReActAgent.builder()
                .name("ES_RAG_Agent")
                .sysPrompt("你是一个有用的助手，可以访问 Elasticsearch"
                        + " 知识库。当用户提出技术问题时，使用"
                        + " retrieve_knowledge 工具在数据库中查找答案。"
                        + "如果可能，请始终注明你的信息来源。")
                .model(
                        DashScopeChatModel.builder()
                                .apiKey(apiKey)
                                .modelName("qwen-max")
                                .stream(true)
                                .enableThinking(true)
                                .formatter(new DashScopeChatFormatter())
                                .build()

                )
                .toolkit(new Toolkit())
                .memory(new InMemoryMemory())
                .knowledge(knowledge)
                .ragMode(RAGMode.AGENTIC)
                .build();


        System.out.println("开始进行知识检索");
        System.out.println("agentScope是什么");
        System.out.println("如何使用 es");
        System.out.println("rag");

        // Start interactive chat
        ExampleUtils.startChat(agent);
    }


}
