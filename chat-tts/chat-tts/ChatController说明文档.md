# ChatController 代码说明文档

## 概述

`ChatController` 是一个基于 Spring Boot WebFlux 的 REST 控制器，实现了**文本对话 + 实时语音合成（TTS）** 的联动功能。

用户发送文本消息后，后端通过 AgentScope 框架调用大语言模型（LLM）生成回答，同时将生成的文本实时传入语音合成服务，最终通过 **SSE（Server-Sent Events）** 流式返回文本片段和 Base64 编码的音频数据。

---

## 技术栈

| 组件 | 说明 |
|------|------|
| Spring Boot WebFlux | 响应式 Web 框架，支持 SSE 流式输出 |
| AgentScope (`io.agentscope`) | AI Agent 编排框架，版本 `1.0.10` |
| DashScope SDK | 阿里云大模型 + TTS 服务 SDK，版本 `2.15.0` |
| Reactor (`reactor.core`) | 响应式流库（Flux / Sinks） |
| Java 17 | 运行时版本 |

---

## 类结构

```
com.coderpwh.chattts.controller
└── ChatController
    ├── 字段：DashScopeChatModel chatModel
    ├── 字段：String apiKey
    ├── 构造函数：ChatController()
    └── 接口：POST/GET /api/chat → Flux<ServerSentEvent>
```

---

## 核心字段

| 字段 | 类型 | 说明 |
|------|------|------|
| `chatModel` | `DashScopeChatModel` | 对话模型实例，使用 `qwen-plus` 模型 |
| `apiKey` | `String` | DashScope API 密钥，从环境变量读取 |

---

## 构造函数

```java
public ChatController() {
    String apiKey = System.getenv("DASHSCOPE_API_KEY");
    if (apiKey == null || apiKey.isEmpty()) {
        throw new IllegalStateException("DASHSCOPE_API_KEY environment variable is required");
    }
    this.apiKey = apiKey;
    this.chatModel = DashScopeChatModel.builder()
        .apiKey(apiKey)
        .modelName("qwen-plus")
        .build();
}
```

**要点：**
- 启动时从系统环境变量 `DASHSCOPE_API_KEY` 读取密钥，若未配置直接抛出异常，防止服务带着错误配置启动。
- `DashScopeChatModel` 使用 `qwen-plus` 模型作为对话 LLM。

---

## 接口说明

### `POST /api/chat`

| 属性 | 值 |
|------|----|
| 路径 | `/api/chat` |
| 跨域 | 允许所有来源（`@CrossOrigin(origins = "*")`） |
| 请求体 | `application/json`，字段 `message`（用户文本） |
| 响应类型 | `text/event-stream`（SSE 流） |
| 返回类型 | `Flux<ServerSentEvent<Map<String, Object>>>` |

#### 请求示例

```json
{
  "message": "你好，请介绍一下自己"
}
```

#### SSE 事件类型

| 事件名 | 触发时机 | 数据结构 |
|--------|----------|----------|
| `text` | 模型每生成一段文本 | `{"text": "text", "isLast": false/true}` |
| `audio` | TTS 合成出一段音频 | `{"audio": "<base64编码的PCM数据>"}` |
| `done` | 全部生成完成 | `{"status": "completed"}` |
| `error` | 发生异常 | `{"error": "<错误信息>"}` |

---

## 核心流程

```
客户端 POST /api/chat
        │
        ▼
  参数校验（message 非空）
        │
        ▼
  构建 DashScopeRealtimeTTSModel
  （模型: qwen3-tts-flash-realtime，声音: Cherry，采样率: 24000Hz，格式: PCM）
        │
        ▼
  构建 TTSHook
  （绑定 audioCallback → 收到音频 → 推送 audio 事件到 Sinks）
        │
        ▼
  构建 ReActAgent
  （系统提示: "你是一个友好的中文助手"，最大迭代: 3，挂载 TTSHook）
        │
        ▼
  构建用户 Msg，调用 agent.stream()
  （流式订阅，开启 REASONING 事件，增量模式）
        │
  ┌─────┴─────┐
  │           │
doOnNext   doOnComplete / doOnError
（推送       （停止 TTS，关闭 WebSocket，
text 事件）   推送 done/error 事件，关闭 Sinks）
        │
        ▼
  sink.asFlux() 返回给客户端（SSE）
  （客户端断开时 doOnCancel 清理资源）
```

---

## 关键组件详解

### 1. Sinks（响应式桥接）

```java
Sinks.Many<ServerSentEvent<Map<String, Object>>> sink =
    Sinks.many().multicast().onBackpressureBuffer();
```

- 用于在非响应式的回调（`audioCallback`、`doOnNext`）中向响应式流推送数据。
- `multicast().onBackpressureBuffer()` 支持多订阅者并缓冲背压。

### 2. DashScopeRealtimeTTSModel（实时 TTS）

```java
DashScopeRealtimeTTSModel requestTtsModel = DashScopeRealtimeTTSModel.builder()
    .apiKey(apiKey)
    .modelName("qwen3-tts-flash-realtime")
    .voice("Cherry")          // 音色：Cherry
    .sampleRate(24000)        // 采样率：24000 Hz
    .format("pcm")            // 音频格式：PCM 裸数据
    .build();
```

每次请求独立创建一个 TTS 模型实例，避免多请求之间的状态污染。

### 3. TTSHook（文本转语音钩子）

```java
TTSHook ttsHook = TTSHook.builder()
    .ttsModel(requestTtsModel)
    .audioCallback(audio -> {
        if (audio.getSource() instanceof Base64Source src) {
            sink.tryEmitNext(/* audio 事件 */);
        }
    })
    .build();
```

- Hook 在 Agent 生成文本时被触发，将文本发送给 TTS 服务。
- 收到 TTS 返回的 Base64 音频数据后，通过 `sink` 推送 `audio` SSE 事件。

### 4. ReActAgent（推理 Agent）

```java
ReActAgent agent = ReActAgent.builder()
    .name("Assistant")
    .sysPrompt("你是一个友好的中文助手.请用简洁的中文回答问题")
    .model(chatModel)
    .hook(ttsHook)
    .maxIters(3)   // 最大推理迭代轮数
    .build();
```

- 使用 ReAct（Reasoning + Acting）模式，支持工具调用与推理链。
- `maxIters(3)` 限制最大推理步骤，防止无限循环。

### 5. 流式订阅与资源清理

```java
agent.stream(userMsg, StreamOptions.builder()
        .eventTypes(EventType.REASONING)
        .incremental(true)
        .build())
    .subscribeOn(Schedulers.boundedElastic())  // 在弹性线程池中订阅
    .doOnNext(event -> { /* 推送 text 事件 */ })
    .doOnComplete(() -> {
        ttsHook.stop();
        requestTtsModel.close();  // 关闭 WebSocket 连接
        sink.tryEmitNext(/* done 事件 */);
        sink.tryEmitComplete();
    })
    .doOnError(e -> {
        ttsHook.stop();
        requestTtsModel.close();
        sink.tryEmitNext(/* error 事件 */);
        sink.tryEmitComplete();
    })
    .subscribe();
```

- `Schedulers.boundedElastic()` 将阻塞操作调度到弹性线程池，不阻塞 Netty 事件循环。
- 完成/错误/客户端取消三种路径均需清理 TTS 资源。

---

## 资源清理策略

| 场景 | 处理方式 |
|------|----------|
| 正常完成 | `doOnComplete` → `ttsHook.stop()` + `requestTtsModel.close()` |
| 发生异常 | `doOnError` → `ttsHook.stop()` + `requestTtsModel.close()` |
| 客户端主动断开 | `doOnCancel` → `ttsHook.stop()` + `requestTtsModel.close()` |

---

## 注意事项 / 潜在问题

1. **text 事件数据 bug**：`doOnNext` 中推送的 text 事件，`data` 字段硬编码了字符串 `"text"` 而非实际内容：
   ```java
   // 当前代码（有误）
   .data(Map.of("text", "text", "isLast", evnet.isLast()))
   //                     ^^^^^ 应改为 text 变量
   
   // 应改为
   .data(Map.of("text", text, "isLast", evnet.isLast()))
   ```

2. **每次请求创建新的 TTS 实例**：高并发场景下会有大量 WebSocket 连接开销，需关注连接池使用。

3. **跨域设置**：`@CrossOrigin(origins = "*")` 允许所有来源，生产环境建议限制为具体域名。

4. **环境变量依赖**：服务启动强依赖 `DASHSCOPE_API_KEY` 环境变量，部署时需确保已配置。

---

## 环境配置

```bash
# 必须配置的环境变量
export DASHSCOPE_API_KEY=your_dashscope_api_key_here
```

---

## 快速测试

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "你好"}' \
  --no-buffer
```

预期输出（SSE 流）：
```
event: text
data: {"text":"你好！我是你的中文助手...","isLast":false}

event: audio
data: {"audio":"<base64编码PCM数据>"}

event: done
data: {"status":"completed"}
```
