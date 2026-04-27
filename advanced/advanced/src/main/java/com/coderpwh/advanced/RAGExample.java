package com.coderpwh.advanced;

import io.agentscope.core.embedding.EmbeddingModel;
import io.agentscope.core.embedding.dashscope.DashScopeTextEmbedding;
import io.agentscope.core.rag.Knowledge;
import io.agentscope.core.rag.knowledge.SimpleKnowledge;
import io.agentscope.core.rag.store.InMemoryStore;
import io.agentscope.core.rag.store.VDBStoreBase;

public class RAGExample {
    private static final int EMBEDDING_DIMENSIONS = 1024;

    public static void main(String[] args) {

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

        Knowledge knowledge  = SimpleKnowledge.builder()
                .embeddingModel(embeddingModel)
                .embeddingStore(vdbStoreBase)
                .build();

        System.out.println("知识库创建完毕");

        System.out.println("开始进行知识添加");

    }

    private static  void addSampleDocuments(Knowledge knowledge) {

    }


}
