# ElasticsearchRAGExample 技术知识点整理

## 概述

`ElasticsearchRAGExample` 是基于 **AgentScope** 框架与 **Elasticsearch 向量存储** 的 **RAG（检索增强生成）** 示例程序。

它演示了如何：

1. 通过 **DashScope text-embedding-v3** 模型把文本转换为稠密向量（dense vector）；
2. 把向量化后的文档持久化到 **Elasticsearch** 索引中，作为外部知识库；
3. 构建一个具备 **AGENTIC RAG** 能力的 `ReActAgent`，在对话过程中由 Agent 自主决定何时检索知识库；
4. 借助 ReAct（Reasoning + Acting）模式与 `qwen-max` 大模型进行多轮交互。

---

## 技术栈

| 组件 | 版本 | 说明 |
|------|------|------|
| AgentScope Core (`io.agentscope:agentscope-core`) | 1.0.10 | 多智能体编排框架，提供 `ReActAgent`、`Knowledge`、`EmbeddingModel` 等核心抽象 |
| AgentScope RAG Simple (`agentscope-extensions-rag-simple`) | 1.0.10 | 提供 `SimpleKnowledge` 实现 |
| Elasticsearch Java Client (`co.elastic.clients:elasticsearch-java`) | 9.3.3 | 与 ES 通信的官方 Java 客户端 |
| DashScope SDK | 2.15.0 | 阿里云灵积模型服务，提供 LLM 与 Embedding |
| Spring Boot BOM | 4.0.1 | 仅做依赖版本管理 |
| Java | 17 | 运行时版本 |

---

## 整体架构

```
                  ┌────────────────────────────────────────┐
                  │            用户 (CLI 输入)              │
                  └───────────────┬────────────────────────┘
                                  │ Msg
                                  ▼
                  ┌────────────────────────────────────────┐
                  │  ReActAgent (qwen-max + ReAct 推理)     │
                  │  - sysPrompt：让模型在需要时调用工具     │
                  │  - RAGMode.AGENTIC                     │
                  └───────┬─────────────────────┬──────────┘
                          │                     │
                工具调用：retrieve_knowledge      │ 直接回答
                          │                     │
                          ▼                     ▼
              ┌────────────────────┐    ┌────────────────┐
              │   Knowledge        │    │   LLM 输出      │
              │ (SimpleKnowledge)  │    └────────────────┘
              │  - embeddingModel  │
              │  - embeddingStore  │
              └────────┬───────────┘
                       │ embed + kNN search
                       ▼
              ┌────────────────────┐    ┌──────────────────────────┐
              │ ElasticsearchStore │◀──▶│  Elasticsearch（向量索引）│
              │  index: agentscope_│    │  field: dense_vector      │
              │        rag_example │    │  search: kNN              │
              └────────────────────┘    └──────────────────────────┘
                       ▲
                       │ embed
                       │
              ┌────────┴───────────┐
              │  EmbeddingModel    │
              │ DashScopeText      │
              │  Embedding (v3)    │
              │  dimensions=1024   │
              └────────────────────┘
```

---

## 关键配置常量

| 常量 | 值 | 说明 |
|------|----|------|
| `EMBEDDING_DIMENSIONS` | `1024` | DashScope `text-embedding-v3` 的向量维度，必须与 ES mapping 一致 |
| `ES_URL` | `http://localhost:9200` | Elasticsearch 访问地址 |
| `ES_USERNAME` / `ES_PASSWORD` | 由 `-Des.user`、`-Des.pass` 注入 | 启用 ES 鉴权时使用 |
| `ES_INDEX_NAME` | `agentscope_rag_example` | 用于存储向量化文档的索引名 |

> ⚠️ **注意**：`EMBEDDING_DIMENSIONS` 在「Embedding 模型」与「向量存储」两侧必须保持一致，否则写入或检索时会报维度错误。

---

## 主要流程拆解

`main` 方法的整体流程可以拆为 **5 步**。

### Step 1. 读取 API Key

```java
String apikey = ExampleUtils.getDashScopeApiKey();
```

通过 `ExampleUtils` 读取 DashScope API Key（默认从环境变量 `DASHSCOPE_API_KEY` 中获取，当前代码中写死为 `""` 用于本地测试）。

### Step 2. 创建 Embedding 模型

```java
EmbeddingModel embeddingModel = DashScopeTextEmbedding.builder()
        .apiKey(apikey)
        .modelName("text-embedding-v3")
        .dimensions(EMBEDDING_DIMENSIONS) // 1024
        .build();
```

知识点：

- `EmbeddingModel` 是 AgentScope 抽象出来的 **嵌入模型接口**，屏蔽底层实现差异。
- `text-embedding-v3` 支持自定义维度（`dimensions`），常见取值为 `512 / 768 / 1024 / 1536`，本例固定为 `1024`。
- 同一份知识库在 **写入** 与 **检索** 阶段必须使用 **相同的 Embedding 模型与维度**，否则向量空间不一致，检索结果无意义。

### Step 3. 创建 Elasticsearch 向量存储

```java
try (ElasticsearchStore elasticsearchStore = ElasticsearchStore.builder()
        .url("http://localhost:9200")
        // .username("")
        // .password("")
        .indexName(ES_INDEX_NAME)
        .dimensions(EMBEDDING_DIMENSIONS)
        .build()) {
    ...
}
```

知识点：

- `ElasticsearchStore` 实现了 AgentScope 的 `VDBStoreBase`（向量数据库存储接口）契约，可以与 `InMemoryStore`、其他向量库实现（如 Bailian、Milvus 等）互换。
- `try-with-resources` 语法保证 ES 客户端连接在退出时自动关闭，**避免资源泄漏**。
- 首次运行时若索引不存在，`ElasticsearchStore` 会按 `dimensions` 自动创建对应 mapping（`dense_vector` 类型 + 索引选项），后续运行直接复用。

### Step 4. 构建 Knowledge 并写入文档

```java
Knowledge knowledge = SimpleKnowledge.builder()
        .embeddingModel(embeddingModel)
        .embeddingStore(elasticsearchStore)
        .build();

addSampleDocuments(knowledge);
```

`Knowledge` 是 AgentScope RAG 的核心对外抽象：

| 能力 | 说明 |
|------|------|
| `addDocuments()` | 把 `Document` 列表向量化后写入向量存储 |
| `retrieve()` | 给定 query，向量化后从向量存储中召回 Top-K 相关文档 |
| `embeddingModel` | 负责把文本 → 向量 |
| `embeddingStore` | 负责持久化 & 相似度检索 |

#### addSampleDocuments 详解

```java
TextReader reader = new TextReader(512, SplitStrategy.PARAGRAPH, 50);

for (int i = 0; i < documents.length; i++) {
    String docText = documents[i];
    ReaderInput input = ReaderInput.fromString(docText);

    List<Document> docs = reader.read(input).block();
    if (docs != null && !docs.isEmpty()) {
        knowledge.addDocuments(docs).block();
    }
}
```

| 参数 / 调用 | 含义 |
|-------------|------|
| `TextReader(512, SplitStrategy.PARAGRAPH, 50)` | 文本切分器：`chunkSize=512`、按 `PARAGRAPH` 段落策略切分、`overlap=50`（相邻 chunk 之间保留 50 字符重叠以防语义断裂）|
| `ReaderInput.fromString(...)` | 将原始字符串包装为 `ReaderInput` |
| `reader.read(input).block()` | 异步切分文档（返回 `Mono<List<Document>>`），`block()` 阻塞拿到结果 |
| `knowledge.addDocuments(docs).block()` | 调用 Embedding 模型把每个 `Document` 向量化，并写入 Elasticsearch |

> 💡 **响应式编程要点**：AgentScope 内部基于 Reactor（`Mono` / `Flux`）。示例代码使用 `block()` 把响应式调用转为同步调用，便于在脚本式 main 方法中演示，**生产代码中应尽量保留响应式链路**。

### Step 5. 构建 ReActAgent 并启动对话

```java
ReActAgent agent = ReActAgent.builder()
        .name("ES_RAG_Agent")
        .sysPrompt("你是一个有用的助手，可以访问 Elasticsearch ..."
                 + " 当用户提出技术问题时，使用 retrieve_knowledge 工具在数据库中查找答案。"
                 + " 如果可能，请始终注明你的信息来源。")
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
```

知识点：

- **ReActAgent**：AgentScope 中实现 ReAct（**Reasoning + Acting**）模式的 Agent，支持「思考 → 调用工具 → 再思考 → 输出」的循环。
- **`sysPrompt`**：通过提示词显式引导模型在需要外部知识时调用 `retrieve_knowledge` 工具。
- **`stream(true)`**：开启流式输出，token 边生成边返回。
- **`enableThinking(true)`**：启用 qwen-max 的「思维链」内部推理能力（thinking）。
- **`formatter(new DashScopeChatFormatter())`**：负责把 AgentScope 的 `Msg` 模型 ↔ DashScope SDK 的请求/响应格式互转。
- **`toolkit(new Toolkit())`**：空工具集——`retrieve_knowledge` 由 `Knowledge` + `RAGMode` 自动注入，无需手动添加。
- **`memory(new InMemoryMemory())`**：会话短期记忆，存储在内存中，重启即丢失。
- **`knowledge(knowledge)`**：把外部知识库挂载到 Agent。
- **`ragMode(RAGMode.AGENTIC)`**：见下文。

---

## RAG 模式：AGENTIC vs 其他模式

| 模式 | 特点 | 适用场景 |
|------|------|----------|
| `AGENTIC`（本例） | 模型自己决定 **是否** 以及 **何时** 调用 `retrieve_knowledge`，可以多次检索、按需检索 | 对话型问答、复杂多步推理 |
| 自动注入式（如 `INJECTED`） | 每轮把 Top-K 文档强制塞进上下文 | 简单 FAQ、固定问答 |
| 关闭模式 | 不使用知识库 | 纯闲聊 |

> AGENTIC 模式的好处：**避免无关检索带来的上下文噪声**，对模型的「工具调用决策能力」要求更高。`qwen-max` 配合 ReAct 提示词可以稳定触发。

---

## Elasticsearch 在 RAG 中扮演的角色

| 维度 | 说明 |
|------|------|
| 存储引擎 | 倒排索引 + `dense_vector` 字段 |
| 检索能力 | **kNN（k-nearest neighbors）** 向量相似度搜索（HNSW 图） |
| 优势 | 分布式可扩展、支持向量 + 关键字混合检索（hybrid search）、生态成熟 |
| 与 `InMemoryStore` 对比 | 持久化、可跨进程共享、可水平扩展；代价是部署复杂度 + 网络开销 |

ES 索引生命周期：

1. 程序首次启动 → `ElasticsearchStore` 创建索引（含 `dense_vector` mapping，`dims=1024`）。
2. `addDocuments` 阶段 → 每个 `Document` 经 Embedding 模型转为 1024 维向量，写入索引文档。
3. `retrieve_knowledge` 阶段 → query 文本被 embed 为向量，触发 `knn` 搜索，返回 Top-K 文档。

---

## 异常与资源管理

```java
try (ElasticsearchStore elasticsearchStore = ...) {
    ...
} catch (Exception e) {
    log.error("异常信息为:{}", e.getMessage());
}

System.exit(0);
```

| 设计点 | 说明 |
|--------|------|
| `try-with-resources` | `ElasticsearchStore` 实现了 `AutoCloseable`，自动释放 HTTP 连接池 |
| 全局兜底 catch | 防止异常导致 JVM 崩溃，统一打印日志 |
| `System.exit(0)` | 由于 DashScope / Reactor / ES 客户端都使用了非 daemon 线程池，程序不显式退出会**挂起**。这是异步 SDK 的常见现象。 |

---

## 可优化点 / 注意事项

> 以下是阅读源码后值得改进的工程实践点。

1. **API Key 硬编码**：`ExampleUtils.getDashScopeApiKey()` 当前直接 `apiKey = ""`，仅适合本地调试，**不能提交到 Git**。建议恢复 `System.getenv("DASHSCOPE_API_KEY")` 或使用 Spring 的 `@Value`。
2. **ES 连接信息重复**：`ES_URL` 常量已定义，但 `ElasticsearchStore.builder().url("http://localhost:9200")` 又写死了一次。应统一使用常量。
3. **资源关闭顺序**：`Knowledge` 内部持有 `embeddingStore` 引用，关闭 `ElasticsearchStore` 后再调用 `knowledge` 任何方法都会失败——示例代码刚好不会触发，但生产代码需要谨慎。
4. **`block()` 滥用**：在响应式应用中应避免在 main thread 中频繁 `block()`，否则失去异步优势。
5. **批量写入**：示例对每条文档单独调用 `addDocuments`，更高效的做法是**一次提交多个 `Document`**，减少 ES 网络往返。
6. **重复索引**：当前每次启动都会重新写入相同的 5 条文档，造成 **数据重复**。生产实践中应该用 `documentId` 做幂等写入，或在写入前先 `deleteByQuery`。
7. **错误处理**：`addSampleDocuments` 用 `System.err.println` 打印异常，建议统一改为 `log.error` 配合 SLF4J。

---

## 学习路径建议

如果你刚接触 AgentScope + RAG，建议按下面顺序循序渐进：

1. **理解 Embedding**：先了解什么是稠密向量、为什么相似的文本向量距离近。
2. **跑通 `InMemoryStore` 版本**：用最简单的内存向量存储跑通 `Knowledge.addDocuments + retrieve`。
3. **替换为 Elasticsearch**：阅读本示例，理解 `VDBStoreBase` 抽象的可插拔性。
4. **理解 RAGMode.AGENTIC**：观察 ReActAgent 的日志，看模型何时主动调用 `retrieve_knowledge`。
5. **进一步**：尝试混合检索（BM25 + kNN）、Rerank、多知识库路由等高级用法。

---

## 相关 API / 类速查表

| 类 / 接口 | 所属包 | 作用 |
|-----------|--------|------|
| `ReActAgent` | `io.agentscope.core` | ReAct 模式智能体 |
| `Knowledge` | `io.agentscope.core.rag` | 知识库统一抽象 |
| `SimpleKnowledge` | `io.agentscope.core.rag.knowledge` | `Knowledge` 的简单实现 |
| `RAGMode` | `io.agentscope.core.rag` | RAG 工作模式枚举（AGENTIC 等） |
| `EmbeddingModel` | `io.agentscope.core.embedding` | 嵌入模型接口 |
| `DashScopeTextEmbedding` | `io.agentscope.core.embedding.dashscope` | DashScope 文本嵌入实现 |
| `ElasticsearchStore` | `io.agentscope.core.rag.store` | ES 向量存储实现 |
| `TextReader` | `io.agentscope.core.rag.reader` | 文本切分读取器 |
| `SplitStrategy` | `io.agentscope.core.rag.reader` | 文本切分策略枚举 |
| `Document` | `io.agentscope.core.rag.model` | 文档数据模型 |
| `DashScopeChatModel` | `io.agentscope.core.model` | DashScope 对话模型实现 |
| `DashScopeChatFormatter` | `io.agentscope.core.formatter.dashscope` | DashScope 消息格式转换器 |
| `Toolkit` | `io.agentscope.core.tool` | 工具集合 |
| `InMemoryMemory` | `io.agentscope.core.memory` | 内存型短期记忆 |

---

## 运行方式

1. **启动 Elasticsearch**（≥ 8.x，本示例兼容 9.x 客户端）：

   ```bash
   docker run -d --name es \
     -p 9200:9200 \
     -e "discovery.type=single-node" \
     -e "xpack.security.enabled=false" \
     docker.elastic.co/elasticsearch/elasticsearch:8.13.0
   ```

2. **设置 DashScope API Key**：

   ```bash
   export DASHSCOPE_API_KEY=sk-xxxxxxxx
   ```

   并把 `ExampleUtils.getDashScopeApiKey()` 中的硬编码 `""` 改回 `System.getenv("DASHSCOPE_API_KEY")`。

3. **运行示例**：

   ```bash
   mvn -pl advanced/advanced exec:java \
       -Dexec.mainClass=com.coderpwh.advanced.ElasticsearchRAGExample
   ```

4. **交互**：在 `You:` 提示符下输入问题，例如：

   - `agentScope是什么？`
   - `如何在 AgentScope 中使用 Elasticsearch？`
   - `什么是 RAG？`

   Agent 会根据需要调用 `retrieve_knowledge` 检索 ES 中的文档并基于检索结果回答。

---

## 结语

本示例的核心价值在于演示 AgentScope 的 **「Knowledge + EmbeddingModel + EmbeddingStore + ReActAgent + RAGMode」** 五件套如何组合使用：

- **可插拔**：把 `ElasticsearchStore` 换成 `InMemoryStore`、`MilvusStore` 等只是一行 builder 修改。
- **响应式**：底层基于 Reactor，可平滑接入 WebFlux 类应用。
- **AGENTIC RAG**：让模型自主决定何时检索，避免上下文污染，是构建生产级 RAG 智能体的推荐范式。
