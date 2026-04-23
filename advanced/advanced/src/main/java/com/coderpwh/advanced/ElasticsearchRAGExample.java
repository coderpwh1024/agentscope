package com.coderpwh.advanced;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ElasticsearchRAGExample {
    private static final Logger log = LoggerFactory.getLogger(ElasticsearchRAGExample.class);

    // Configuration
    private static final int EMBEDDING_DIMENSIONS = 1024;

    // Elasticsearch Configuration (Modify these as per your setup)
    private static final String ES_URL = System.getProperty("es.url", "http://localhost:9200");
    private static final String ES_USERNAME = System.getProperty("es.user", "elastic");
    private static final String ES_PASSWORD = System.getProperty("es.pass", "changeme");
    private static final String ES_INDEX_NAME = "agentscope_rag_example";



}
