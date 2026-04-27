package com.coderpwh.advanced;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.embedding.EmbeddingModel;
import io.agentscope.core.embedding.dashscope.DashScopeTextEmbedding;
import io.agentscope.core.formatter.dashscope.DashScopeChatFormatter;
import io.agentscope.core.memory.InMemoryMemory;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.rag.Knowledge;
import io.agentscope.core.rag.RAGMode;
import io.agentscope.core.rag.knowledge.SimpleKnowledge;
import io.agentscope.core.rag.model.Document;
import io.agentscope.core.rag.model.RetrieveConfig;
import io.agentscope.core.rag.reader.ReaderInput;
import io.agentscope.core.rag.reader.TextReader;
import io.agentscope.core.rag.store.InMemoryStore;
import io.agentscope.core.rag.store.VDBStoreBase;
import io.agentscope.core.rag.reader.SplitStrategy;
import io.agentscope.core.tool.Toolkit;
import java.io.IOException;
import java.util.List;

public class RAGExample {
    private static final int EMBEDDING_DIMENSIONS = 1024;

    public static void main(String[] args) throws IOException {

        ExampleUtils.printWelcome(
                "RAG(检索增强生成)示例",
                "本示例演示 RAG 功能:\n"
                        + "  - 创建和填充知识库\n"
                        + "  - 通用模式:自动注入知识\n"
                        + "  - 智能体模式:由智能体控制知识检索");


        String apiKey = ExampleUtils.getDashScopeApiKey();

        System.out.println("开始创建 Embeding 模型了");

        EmbeddingModel embeddingModel = DashScopeTextEmbedding.builder()
                .apiKey(apiKey)
                .modelName("text-embedding-v3")
                .dimensions(EMBEDDING_DIMENSIONS)
                .build();

        System.out.println("创建 embeddingModel 结束");

        System.out.println("开始创建向量存储");

        // 向量
        VDBStoreBase vdbStoreBase = InMemoryStore.builder().dimensions(EMBEDDING_DIMENSIONS).build();
        System.out.println("向量存储创建完毕");

        System.out.println("开始创建知识库");

        Knowledge knowledge = SimpleKnowledge.builder()
                .embeddingModel(embeddingModel)
                .embeddingStore(vdbStoreBase)
                .build();

        System.out.println("知识库创建完毕");
        System.out.println("开始进行知识添加");

        addSampleDocuments(knowledge);
        System.out.println("文档添加完毕");

        System.out.println("Agentic RAG Mode");

        System.out.println(
                "在 Agentic(智能体)模式下,智能体会自行决定何时\n"
                        + "通过 retrieve_knowledge 工具来检索知识。这使智能体\n"
                        + "能够更灵活地控制知识的使用时机。\n");
        demonstrateAgenticMode(apiKey, knowledge);
        System.out.println("\n=== All examples completed ===");
    }


    /***
     * 添加文档
     * @param knowledge
     */
    private static void addSampleDocuments(Knowledge knowledge) {

        String[] documents = {
                "AgentScope is a multi-agent system framework developed by ModelScope. It provides a"
                        + " unified interface for building and managing multi-agent applications."
                        + " AgentScope supports both synchronous and asynchronous agent communication.",
                "AgentScope supports various agent types including ReActAgent, which implements the "
                        + "ReAct (Reasoning and Acting) algorithm. ReActAgent combines reasoning and "
                        + "acting in an iterative loop to solve complex tasks.",
                "RAG (Retrieval-Augmented Generation) is a technique that enhances language models by"
                        + " retrieving relevant information from a knowledge base before generating"
                        + " responses. This allows models to access up-to-date information and reduce"
                        + " hallucinations.",
                "AgentScope Java is the Java implementation of AgentScope framework. It provides "
                        + "reactive programming support using Project Reactor, making it suitable for "
                        + "building scalable multi-agent systems.",
                "Vector stores are used in RAG systems to store and search document embeddings. "
                        + "AgentScope supports in-memory vector stores and can integrate with external "
                        + "vector databases like Qdrant and ChromaDB."
        };

        TextReader reader = new TextReader(512, SplitStrategy.PARAGRAPH, 50);

        for (int i = 0; i < documents.length; i++) {
            String docText = documents[i];
            ReaderInput input = ReaderInput.fromString(docText);
            try {
                List<Document> docs = reader.read(input).block();
                if (docs != null && !docs.isEmpty()) {
                    knowledge.addDocuments(docs).block();
                    System.out.println(
                            "  Added document "
                                    + (i + 1)
                                    + ": "
                                    + docText.substring(0, Math.min(50, docText.length()))
                                    + "...");
                }
            } catch (Exception e) {
                System.out.println("错误信息是:" + e.getMessage());
            }
        }

    }


    private static void demonstrateGenericMode(String apiKey, Knowledge knowledge) throws IOException {
        ReActAgent agent = ReActAgent.builder()
                .name("RAGAssistant")
                .sysPrompt(
                        "你是一个有用的助手,可以访问知识库。"
                                + "请使用提供的知识准确地回答问题。"
                                + "如果知识库中不包含相关信息,"
                                + "请明确说明。")
                .model(
                        DashScopeChatModel.builder()
                                .apiKey(apiKey)
                                .modelName("qwen-max")
                                .stream(true)
                                .enableThinking(false)
                                .formatter(new DashScopeChatFormatter())
                                .build()
                )
                .memory(new InMemoryMemory())
                .toolkit(new Toolkit())
                .knowledge(knowledge)
                .retrieveConfig(RetrieveConfig.builder().limit(3).scoreThreshold(0.3).build())
                .build();


        System.out.println("开始进行通用模式");
        System.out.println("什么是AgentScope?");
        System.out.println("什么是RAG");
        ExampleUtils.startChat(agent);

    }


    /***
     *  演示智能体模式
     * @param apiKey
     * @param knowledge
     * @throws IOException
     */
    private static void demonstrateAgenticMode(String apiKey, Knowledge knowledge) throws IOException {
        ReActAgent agent = ReActAgent.builder()
                .name("RAGAgent")
                .sysPrompt(
                        "你是一个乐于助人的助手,配备了知识检索工具。"
                                + "当你需要从知识库中查找信息时,"
                                + "请调用 retrieve_knowledge 工具。"
                                + "并始终向用户说明你当前的操作。")
                .model(
                        DashScopeChatModel.builder()
                                .apiKey(apiKey)
                                .modelName("qwen-max")
                                .stream(true)
                                .enableThinking(false)
                                .formatter(new DashScopeChatFormatter())
                                .build()
                )
                .toolkit(new Toolkit())
                .memory(new InMemoryMemory())
                .knowledge(knowledge)
                .ragMode(RAGMode.AGENTIC)
                .build();

        System.out.println("开始进行智能体模式");
        System.out.println("agentScope是什么");
        System.out.println("RAG是什么?");
        System.out.println("ReAct Agent 是什么？");

        /**
         * 启动会话
         */
        ExampleUtils.startChat(agent);

    }


}
