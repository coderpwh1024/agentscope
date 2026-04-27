# RAGExample 技术知识点整理

## 概述

`RAGExample` 是基于 **AgentScope** 框架、使用 **内存向量存储（InMemoryStore）** 实现的 **RAG（检索增强生成）** 入门示例。
相比 `ElasticsearchRAGExample` 使用外部 ES 作为向量库，本示例完全在 JVM 进程内部完成向量的存储与检索，无需任何外部依赖，便于在本地快速跑通 RAG 全流程。

它演示了如何：

1. 通过 **DashScope `text-embedding-v3`** 模型把文本转换为 1024 维稠密向量；
2. 把向量化后的文档保存到 **内存向量库（InMemoryStore）** 中作为 Knowledge；
3. 使用 **TextReader + 段落切分策略（PARAGRAPH）** 把原始文档切片为可检索的 `Document`；
4. 构建一个 `ReActAgent`，并通过两种 RAG 模式之一与用户对话：
   - **通用模式（Generic Mode）**：每次对话前由框架自动检索知识并注入上下文；
   - **智能体模式（Agentic Mode）**：由 Agent 自行决定何时调用 `retrieve_knowledge` 工具检索知识；
5. 借助 ReAct（Reasoning + Acting）循环与 `qwen-max` 大模型完成多轮对话。

---

## 技术栈

| 组件 | 说明 |
|------|------|
| AgentScope Core (`io.agentscope:agentscope-core`) | 多智能体编排框架，提供 `ReActAgent`、`Knowledge`、`EmbeddingModel`、`RAGMode` 等核心抽象 |
| AgentScope RAG Simple (`agentscope-extensions-rag-simple`) | 提供 `SimpleKnowledge`、`InMemoryStore`、`TextReader` 等 RAG 默认实现 |
| DashScope SDK | 阿里云灵积模型服务，提供 LLM (`qwen-max`) 与 Embedding (`text-embedding-v3`) |
| Project Reactor | 上层使用 `Mono.block()` 来同步等待异步流完成 |
| Java 17 | 运行时版本 |

---

## 整体架构

```
                ┌──────────────────────────────────────────┐
                │              用户 (CLI 输入)              │
                └─────────────────────┬────────────────────┘
                                      │ Msg
                                      ▼
                ┌──────────────────────────────────────────┐
                │  ReActAgent (qwen-max + ReAct 推理)       │
                │  - sysPrompt                             │
                │  - InMemoryMemory (会话短期记忆)          │
                │  - Toolkit (工具集)                       │
                │  - knowledge (绑定的知识库)               │
                │  - RAGMode: GENERIC / AGENTIC            │
                └────────┬─────────────────────┬──────────┘
                         │                     │
        AGENTIC: 工具调用 │                     │ GENERIC: 由框架自动检索
        retrieve_knowledge│                     │ 并把命中文档注入 prompt
                         ▼                     ▼
                ┌────────────────────────────────────────┐
                │  Knowledge (SimpleKnowledge)            │
                │   - embeddingModel                     │
                │   - embeddingStore                     │
                └────────┬─────────────────────┬─────────┘
                         │ embed               │ 存/取向量
                         ▼                     ▼
                ┌──────────────────┐    ┌────────────────────┐
                │  EmbeddingModel  │    │  InMemoryStore      │
                │ DashScopeText    │    │  dimensions=1024    │
                │  Embedding (v3)  │    │  (进程内向量索引)    │
                │  dim = 1024      │    └────────────────────┘
                └──────────────────┘             ▲
                                                 │
                                  ┌──────────────┴────────────────┐
                                  │ TextReader (chunk 512,        │
                                  │   PARAGRAPH 切分, overlap 50) │
                                  └───────────────────────────────┘
```

---

## 关键配置常量

| 常量 | 值 | 说明 |
|------|----|------|
| `EMBEDDING_DIMENSIONS` | `1024` | DashScope `text-embedding-v3` 的向量维度，必须与 `InMemoryStore` 的 `dimensions` 严格一致 |
| `chunkSize`（TextReader 第 1 个参数） | `512` | 每个切片的最大字符数 |
| `SplitStrategy.PARAGRAPH` | — | 按段落切分；适合短文档与按自然段落组织语义的场景 |
| `chunkOverlap`（TextReader 第 3 个参数） | `50` | 相邻切片之间的重叠字符数，避免语义被硬切断 |
| `RetrieveConfig.limit` | `3` | Top-K 检索条数 |
| `RetrieveConfig.scoreThreshold` | `0.3` | 相似度阈值，过滤弱相关结果 |

---

## 主流程拆解（`main` 方法）

```java
public static void main(String[] args) throws IOException {
    ExampleUtils.printWelcome(...);                       // 1. 打印示例标题
    String apiKey = ExampleUtils.getDashScopeApiKey();    // 2. 从环境变量读取 DASHSCOPE_API_KEY

    // 3. 创建 Embedding 模型
    EmbeddingModel embeddingModel = DashScopeTextEmbedding.builder()
            .apiKey(apiKey)
            .modelName("text-embedding-v3")
            .dimensions(EMBEDDING_DIMENSIONS)
            .build();

    // 4. 创建内存向量存储（dimension 必须与上面对齐）
    VDBStoreBase vdbStoreBase = InMemoryStore.builder()
            .dimensions(EMBEDDING_DIMENSIONS).build();

    // 5. 用 embeddingModel + vdbStoreBase 组装 SimpleKnowledge
    Knowledge knowledge = SimpleKnowledge.builder()
            .embeddingModel(embeddingModel)
            .embeddingStore(vdbStoreBase)
            .build();

    // 6. 把示例文档切片、向量化并写入知识库
    addSampleDocuments(knowledge);

    // 7. 演示 Agentic（智能体）RAG 模式
    demonstrateAgenticMode(apiKey, knowledge);
}
```

执行顺序与依赖关系：**Embedding 模型 → 向量库 → Knowledge → 文档入库 → Agent → 对话**。
Embedding 模型与向量库的 `dimensions` 不一致会在写入时直接报错，是新手最常踩的坑。

---

## 知识入库：`addSampleDocuments(Knowledge)`

```java
TextReader reader = new TextReader(512, SplitStrategy.PARAGRAPH, 50);

for (String docText : documents) {
    ReaderInput input = ReaderInput.fromString(docText);
    List<Document> docs = reader.read(input).block();   // 1. 切片
    if (docs != null && !docs.isEmpty()) {
        knowledge.addDocuments(docs).block();           // 2. 向量化 + 写入向量库
    }
}
```

技术要点：

- **`TextReader`**：文本读取器，负责把一段原始字符串切分为多个 `Document` 切片。
  - 第 1 个参数 `chunkSize=512`：单个切片的最大字符长度。
  - 第 2 个参数 `SplitStrategy.PARAGRAPH`：按段落切分。其它常见策略一般还有 `SENTENCE`、`CHARACTER` 等，可按文档结构选择。
  - 第 3 个参数 `chunkOverlap=50`：相邻切片重叠 50 个字符，缓解"边界处语义被截断"导致召回率下降。
- **`ReaderInput.fromString(text)`**：把字符串包装成 Reader 的输入；同类工厂方法通常还支持文件、URL 等。
- **响应式 API**：`reader.read(input)` 与 `knowledge.addDocuments(docs)` 都返回 Reactor 的 `Mono`/`Flux`，示例通过 `.block()` 在 `main` 里同步等待结果，便于命令行交互。
- **入库过程**：`SimpleKnowledge.addDocuments` 内部会调用 `embeddingModel` 把每条 `Document` 编码为 1024 维向量，然后写入 `InMemoryStore`。

---

## 两种 RAG 模式

AgentScope 通过 `RAGMode` 枚举抽象出两种把知识注入到 LLM 推理过程的方式。本示例分别用两个方法演示。

### 1. 通用模式（GENERIC）—— `demonstrateGenericMode`

```java
ReActAgent agent = ReActAgent.builder()
        .name("RAGAssistant")
        .sysPrompt("...如果知识库中不包含相关信息,请明确说明。")
        .model(DashScopeChatModel.builder()
                .apiKey(apiKey)
                .modelName("qwen-max")
                .stream(true)
                .enableThinking(false)
                .formatter(new DashScopeChatFormatter())
                .build())
        .memory(new InMemoryMemory())
        .toolkit(new Toolkit())
        .knowledge(knowledge)
        .retrieveConfig(RetrieveConfig.builder()
                .limit(3)
                .scoreThreshold(0.3)
                .build())
        .build();
```

特点：

- **未显式设置 `ragMode`** → 走默认的通用模式；
- 框架在每一轮对话中**自动**用用户问题去 `knowledge` 里做检索；
- `RetrieveConfig` 控制召回行为：
  - `limit=3`：取相似度 Top-3；
  - `scoreThreshold=0.3`：低于该相似度的结果丢弃，避免给 LLM 灌噪声；
- 召回的 `Document` 会拼到 prompt 中再交给 LLM 生成答案，类似经典的 "Retrieve → Stuff → Generate" 流水线。

适用场景：希望"无脑"在每次提问前都先查一下知识库的标准 QA 场景。

> 注意：当前 `main` 方法默认调用的是 Agentic 模式，`demonstrateGenericMode` 没有被直接调用，可按需切换。

### 2. 智能体模式（AGENTIC）—— `demonstrateAgenticMode`

```java
ReActAgent agent = ReActAgent.builder()
        .name("RAGAgent")
        .sysPrompt("当你需要从知识库中查找信息时,请调用 retrieve_knowledge 工具。")
        .model(...同上...)
        .toolkit(new Toolkit())
        .memory(new InMemoryMemory())
        .knowledge(knowledge)
        .ragMode(RAGMode.AGENTIC)
        .build();
```

特点：

- 显式设置 `ragMode(RAGMode.AGENTIC)`；
- 框架会把检索能力注册成一个 **`retrieve_knowledge` 工具**，挂到 `Toolkit` 上；
- 是否检索、检索什么 query、用几次，全部由 **LLM 在 ReAct 循环里自主决定**：
  - **Reasoning**：LLM 先思考"我现在需要外部知识吗？"；
  - **Acting**：若需要，则发起一次 `retrieve_knowledge` 工具调用；
  - 工具结果回流后继续推理，直到产出最终答复；
- 比通用模式更灵活：闲聊不必检索、复杂问题可以多轮检索，能省 token 也能提质量。

适用场景：希望 Agent 自己判断检索时机；或者业务里检索成本高、不想每轮都触发。

---

## ReActAgent 关键构造参数

| 参数 | 说明 |
|------|------|
| `name` | Agent 名称，用于日志和多智能体场景下的区分 |
| `sysPrompt` | 系统提示词，决定 Agent 的角色与行为约束（是否调用工具、如何引用知识等） |
| `model` | 底层 LLM，本例为 `DashScopeChatModel` 包装的 `qwen-max`，开启 `stream=true` 流式输出 |
| `formatter` | `DashScopeChatFormatter`：把 `Msg` 列表转换为 DashScope ChatCompletion 所需的请求格式 |
| `memory` | `InMemoryMemory`：进程内的会话短期记忆，保存多轮对话上下文 |
| `toolkit` | `Toolkit`：工具集合；AGENTIC 模式下 `retrieve_knowledge` 会被自动注册进来 |
| `knowledge` | 绑定的 `Knowledge` 实例，是 RAG 的数据源 |
| `retrieveConfig` | 仅 GENERIC 模式生效，控制召回 Top-K 与相似度阈值 |
| `ragMode` | 缺省为通用模式；显式设置 `RAGMode.AGENTIC` 切换为智能体模式 |

`enableThinking(false)` 表示关闭模型的"思考过程"输出，避免在终端打印中间链式推理。

---

## 涉及到的技术知识点速查

### 1. RAG（Retrieval-Augmented Generation）
- 通过把外部知识检索结果注入 prompt，缓解 LLM 的"幻觉"和知识过期问题；
- 核心三件套：**Embedding 模型 + 向量库 + 检索器**。

### 2. Embedding 与向量维度
- 文本被映射成定长稠密向量（本例 1024 维）；
- **dimension 必须在「Embedding 模型」「向量库」「索引 mapping」三处保持一致**，否则写入或检索阶段报错；
- DashScope `text-embedding-v3` 是阿里云提供的中文友好 Embedding 模型。

### 3. 向量数据库（VDB）
- 抽象基类：`VDBStoreBase`；
- 本示例使用 `InMemoryStore`，所有向量保存在 JVM 内存中，进程重启即丢失，仅适合演示和单测；
- 生产场景常用：Elasticsearch（dense_vector + kNN）、Qdrant、ChromaDB、Milvus、PGVector 等。

### 4. 文档切片（Chunking）
- 切片粒度直接影响召回效果：太大召回不精，太小丢失上下文；
- 常见策略：按段落 / 句子 / 字符长度切；本例用 `SplitStrategy.PARAGRAPH`；
- `chunkOverlap` 用于在相邻切片间保留重叠，缓解"边界截断"。

### 5. Knowledge 抽象
- `Knowledge` 是 AgentScope 对"知识库"的统一接口：`addDocuments(...)`、`retrieve(query, config)` 等；
- `SimpleKnowledge` 是默认实现，按需注入 `embeddingModel` + `embeddingStore` 即可用。

### 6. ReAct Agent
- ReAct = **Reasoning + Acting**：模型在每一步先推理再决定是否调用工具；
- 通过工具调用扩展 LLM 的能力边界（数据库查询、搜索、计算、本例的 `retrieve_knowledge` 等）；
- AgentScope 的 `ReActAgent` 内部封装了多轮 LLM 调用与工具调度循环。

### 7. RAGMode：GENERIC vs AGENTIC
| 维度 | GENERIC | AGENTIC |
|------|---------|---------|
| 检索时机 | 每轮对话前框架自动检索 | 由 LLM 自主决定 |
| 是否消耗工具调用 | 否 | 是（占用 ReAct 工具调用槽位） |
| 适合场景 | 标准 RAG QA | 复杂任务 / 需要选择性检索 |
| 配置入口 | `retrieveConfig` | 工具描述 + sysPrompt 引导 |

### 8. 流式输出 & Reactor 编程
- `DashScopeChatModel.stream(true)`：以 SSE 流式返回 token；
- 上层的 `reader.read(...)`、`knowledge.addDocuments(...)`、`agent.call(...)` 都返回 `Mono`/`Flux`；
- 命令行示例里通过 `.block()` 同步等待结果；在真实服务里建议保留响应式链路，避免阻塞线程。

### 9. 会话记忆 `InMemoryMemory`
- 维护当前会话的多轮 `Msg` 列表，让 Agent "记得"上文；
- 与"知识库"是两个不同概念：知识库是**长期外部知识**，记忆是**短期对话上下文**。

### 10. 配置与工具方法
- `ExampleUtils.getDashScopeApiKey()` 从环境变量 `DASHSCOPE_API_KEY` 读取 Key，未设置则直接退出；
- `ExampleUtils.printWelcome(title, desc)` 打印分隔符美化输出；
- `ExampleUtils.startChat(agent)` 启动一个 `BufferedReader` 循环读取标准输入，构造 `Msg` 调用 `agent.call(userMsg).block()` 并打印响应。

---

## 运行方式

```bash
# 1. 设置 DashScope API Key
export DASHSCOPE_API_KEY=your_api_key_here

# 2. 编译并运行（在项目根目录执行）
mvn -q -pl advanced/advanced exec:java \
    -Dexec.mainClass=com.coderpwh.advanced.RAGExample
```

启动后即可在终端进行多轮对话，建议测试以下问题以观察 RAG 效果：

- `什么是 AgentScope？`
- `RAG 是什么？`
- `ReAct Agent 是什么？`
- `AgentScope 支持哪些向量库？`

输入 `exit` 退出会话。

---

## 常见问题与改进建议

1. **维度不一致报错**：确保 `EMBEDDING_DIMENSIONS` 与所使用 Embedding 模型的实际维度一致（`text-embedding-v3` 默认 1536，本例显式指定为 1024，模型支持时会按指定维度返回）。
2. **进程重启后知识丢失**：`InMemoryStore` 不持久化；要持久化请切换到 `ElasticsearchRAGExample` 风格的外部向量库。
3. **召回噪声大**：调高 `RetrieveConfig.scoreThreshold` 或调小 `limit`，并优化切片策略（合理 `chunkSize` + `chunkOverlap`）。
4. **AGENTIC 模式不调用工具**：检查 `sysPrompt` 是否明确要求"在需要外部信息时调用 `retrieve_knowledge`"，并确认 `Toolkit` 已传入。
5. **生产化建议**：把 `EmbeddingModel`、`Knowledge`、`Agent` 提升为 Spring Bean，统一管理生命周期；切换到响应式 Web 框架，避免在请求线程里 `.block()`。
