package com.coderpwh.advanced;

import io.agentscope.core.model.DashScopeChatModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.agentscope.core.embedding.EmbeddingModel;
import io.agentscope.core.embedding.dashscope.DashScopeTextEmbedding;
import io.agentscope.core.formatter.dashscope.DashScopeChatFormatter;

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



    }


}
