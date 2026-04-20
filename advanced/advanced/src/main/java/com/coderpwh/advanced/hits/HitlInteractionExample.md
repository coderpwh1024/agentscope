# HitlInteractionExample 接口说明

`HitlInteractionExample` 是一个 **Human-In-The-Loop（人机交互）** 的健身教练 Agent 示例，基于 Spring Boot + SSE（Server-Sent Events）流式推送。

Agent 角色：`FitnessCoach`（专业健身教练助手）
注册工具：
- `ask_user`（`UserInteractionTool`）— 动态 UI 交互，用于逐项收集用户信息
- `add_calendar_event`（`AddCalendarEventTool`）— 敏感操作，需要用户确认

底层模型：`DashScopeChatModel`（`qwen-max`，流式开启）
会话存储：`InMemorySession`
运行中的 Agent 保存在 `ConcurrentHashMap<String, ReActAgent> runningAgents` 中，按 `sessionId` 索引。

---

## 一、核心接口

### 1. `POST /api/chat` — 发起对话（流式）

**位置**：`HitlInteractionExample.java:120`

**入参**（JSON Body）：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `sessionId` | String | 会话 ID |
| `message` | String | 用户消息内容 |

**作用**：
1. 创建 / 加载 `ReActAgent`（FitnessCoach），并放入 `runningAgents`。
2. 将用户消息封装为 `Msg`（role=USER，TextBlock 内容）。
3. 调用 `agent.stream(userMsg)`，把 Agent 产生的事件通过 SSE 流式推送给前端。

**返回**：`Flux<ServerSentEvent<Map<String, Object>>>`，事件类型包括：
`TEXT` / `TOOL_USE` / `TOOL_RESULT` / `TOOL_CONFIRM` / `USER_INTERACTION` / `COMPLETE` / `ERROR`。

**异常**：`message` 为空时返回 `ERROR` 事件。

---

### 2. `POST /api/chat/respond` — 用户回答 ask_user

**位置**：`HitlInteractionExample.java:144`

**入参**（JSON Body）：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `sessionId` | String | 会话 ID（默认 `default`） |
| `toolId` | String | 被挂起的 `ask_user` 工具调用 ID |
| `response` | String 或 Object | 用户的回答；表单类型会被 JSON 序列化为字符串 |

**作用**：
1. 把用户回答封装为 `ToolResultBlock`（工具名固定为 `UserInteractionTool.TOOL_NAME`）。
2. 封装到 `Msg`（role=TOOL）回注 Agent。
3. Agent 从挂起状态继续执行（进入下一个问题或生成完整计划）。

**异常**：`toolId` 缺失时返回 `ERROR` 事件。

---

### 3. `DELETE /api/chat/session/{sessionId}` — 清空会话

**位置**：`HitlInteractionExample.java:184`

**入参**：路径参数 `sessionId`。

**作用**：从 `InMemorySession` 删除对应会话键，彻底清空会话上下文（记忆、历史），开启全新对话。

**返回**：`{ "success": true }`

---

### 4. `POST /api/chat/interrupt/{sessionId}` — 中断运行中的 Agent

**位置**：`HitlInteractionExample.java:193`

**入参**：路径参数 `sessionId`。

**作用**：从 `runningAgents` 查找目标 Agent，调用 `agent.interrupt()` 打断当前推理 / 工具调用链。

**返回**：

```json
{ "success": true, "interrupted": true }
```

若未找到对应 Agent，则 `interrupted` 为 `false`。

---

### 5. `POST /api/chat/confirm` — 确认或拒绝敏感工具

**位置**：`HitlInteractionExample.java:204`

**入参**（JSON Body）：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `sessionId` | String | 会话 ID（默认 `default`） |
| `confirmed` | Boolean | `true` 放行，`false` 拒绝 |
| `toolCalls` | List<Map> | 待确认的工具调用列表，每项含 `id`、`name` |
| `reason` | String | 拒绝原因（可选，默认 `Cancelled by user`） |

**作用**：

- **`confirmed = true`**：调用 `agent.stream(StreamOptions.defaults())` 让 Agent 继续执行已挂起的工具（如 `add_calendar_event`）。
- **`confirmed = false`**：为每个待确认工具生成一条 `ToolResultBlock`（内容为拒绝原因），以 TOOL 角色消息回注 Agent，让它据此调整后续行为。

---

## 二、辅助机制

### `wrapAsSSE(sessionId, agent, events)`

**位置**：`HitlInteractionExample.java:267`

统一的 SSE 输出包装：

- 在事件流尾部追加 `COMPLETE` 事件。
- `onErrorResume` 捕获异常，推 `ERROR` + `COMPLETE`。
- `doFinally` 阶段：
  - 从 `runningAgents` 移除当前 Agent
  - 调用 `agent.saveTo(session, sessionId)` 持久化 Agent 状态到 Session

### `convertEvent(Event event)`

**位置**：`HitlInteractionExample.java:278`

将 Agent 内部的 `Event` 翻译为前端可消费的协议：

| Event 类型 | 条件 | 输出事件 |
| --- | --- | --- |
| `REASONING` | 最后一帧 + 含 `ToolUseBlock`，且存在需确认工具 | `TOOL_CONFIRM` |
| `REASONING` | 最后一帧 + 含 `ToolUseBlock`，但工具不需确认 | `TOOL_USE`（过滤掉 `ask_user`） |
| `REASONING` | 普通流式文本 | `TEXT`（`incremental = !isLast`） |
| `TOOL_RESULT` | — | `TOOL_RESULT`（过滤掉 `ask_user`） |
| `AGENT_RESULT` | `GenerateReason == TOOL_SUSPENDED` 且工具为 `ask_user` | `USER_INTERACTION` |
| 其他（HINT/SUMMARY） | — | 忽略 |

### 前端事件协议示例

**`TEXT`**
```json
{ "type": "TEXT", "content": "文本片段", "incremental": true }
```

**`TOOL_USE`**
```json
{ "type": "TOOL_USE", "toolId": "...", "toolName": "...", "toolInput": { } }
```

**`TOOL_RESULT`**
```json
{ "type": "TOOL_RESULT", "toolId": "...", "toolName": "...", "toolResult": "..." }
```

**`TOOL_CONFIRM`**
```json
{
  "type": "TOOL_CONFIRM",
  "pendingToolCalls": [
    { "id": "...", "name": "add_calendar_event", "input": { }, "needsConfirm": true }
  ]
}
```

**`USER_INTERACTION`**
```json
{
  "type": "USER_INTERACTION",
  "toolId": "...",
  "question": "请选择你的健身目标",
  "uiType": "single_choice",
  "options": ["减脂", "增肌", "综合健身", "柔韧性训练"],
  "fields": [],
  "defaultValue": null,
  "allowOther": false
}
```

**`COMPLETE` / `ERROR`**
```json
{ "type": "COMPLETE" }
{ "type": "ERROR", "error": "..." }
```

---

## 三、典型交互流程

```
前端                                    后端 / Agent
 │  POST /api/chat  (sessionId, "我想制定减脂计划")
 │ ─────────────────────────────────────▶
 │                                       创建 / 加载 Agent
 │                                       agent.stream(userMsg)
 │ ◀── SSE: TEXT（推理文本，逐块）
 │ ◀── SSE: USER_INTERACTION（ask_user 挂起：单选健身目标）
 │
 │  用户在 UI 中选择"减脂"
 │  POST /api/chat/respond  (toolId, "减脂")
 │ ─────────────────────────────────────▶
 │                                       ToolResultBlock 回注，Agent 续跑
 │ ◀── SSE: USER_INTERACTION（继续问：身高体重）
 │  ...（循环收集所有信息）
 │
 │                                       Agent 决定调用 add_calendar_event
 │ ◀── SSE: TOOL_CONFIRM（需要用户确认）
 │
 │  用户点"允许"
 │  POST /api/chat/confirm  (confirmed=true)
 │ ─────────────────────────────────────▶
 │                                       Agent 执行工具，生成周计划
 │ ◀── SSE: TOOL_USE / TOOL_RESULT / TEXT
 │ ◀── SSE: COMPLETE
```

---

## 四、关键设计

1. **挂起 / 恢复机制**：`ask_user` 工具触发 Agent 的 `TOOL_SUSPENDED` 状态，前端收到 `USER_INTERACTION` 渲染 UI，用户回答后通过 `/chat/respond` 以 `ToolResultBlock` 续跑。
2. **敏感工具确认**：通过 `ToolConfirmationHook` 在 Agent Hook 层拦截 `TOOLS_REQUIRING_CONFIRMATION` 名单中的工具，emit `TOOL_CONFIRM` 事件由用户决定放行或拒绝。
3. **会话持久化**：每次 SSE 流结束（正常或异常）都会 `agent.saveTo(session, sessionId)`，保证下次请求 `loadIfExists` 能恢复记忆。
4. **可中断**：`/chat/interrupt/{sessionId}` 允许前端随时打断长任务，`runningAgents` 用于按 sessionId 追踪活跃 Agent。

---

## 五、Hook 与 Tool 内部实现

### 1. `ToolConfirmationHook`

**位置**：`ToolConfirmationHook.java`

实现 `io.agentscope.core.hook.Hook` 接口，作用于 **Agent 推理完成之后、真正执行工具之前**。

```java
public ToolConfirmationHook(Set<String> toolsRequiringConfirmation)
```

**关键点**：

- 只处理 `PostReasoningEvent`（模型推理结束事件）。
- 从推理结果 `Msg` 中取出所有 `ToolUseBlock`，命中名单 `toolsRequiringConfirmation` 则调用 `post.stopAgent()` 暂停 Agent。
- Agent 被 stop 后，`HitlInteractionExample.convertEvent` 会在 `REASONING` 分支发出 `TOOL_CONFIRM` 事件，让前端弹出确认框。
- 用户响应通过 `POST /api/chat/confirm` 回注：
  - `confirmed = true` → `agent.stream(StreamOptions.defaults())` 直接续跑（执行已挂起的工具）
  - `confirmed = false` → 为每个工具生成 `ToolResultBlock`（拒绝原因），以 TOOL 消息回注 Agent，绕过真正的工具执行

**本示例中**：只有 `add_calendar_event` 进入名单（`TOOLS_REQUIRING_CONFIRMATION`）。

---

### 2. `ObservationHook`

**位置**：`ObservationHook.java`

观察者 Hook，`priority()` 返回 `900`，覆盖 Agent 生命周期的 7 类事件：

| Hook 事件 | 日志标题 | 说明 |
| --- | --- | --- |
| `PreCallEvent` | `▶ AGENT CALL` | Agent 一次完整运行开始，打印所有输入消息 |
| `PreReasoningEvent` | `🧠 PRE-REASONING` | 调模型前，打印 model 名称 + 消息数 |
| `PostReasoningEvent` | `🧠 POST-REASONING` | 推理完成，打印推理消息、工具调用列表、是否 `isStopRequested` |
| `PreActingEvent` | `🔧 TOOL CALL →` | 工具真正执行前，打印 `id` 与 `input` |
| `PostActingEvent` | `✅ TOOL RESULT ←` | 工具执行结果，标注是否 `suspended`（等待用户输入） |
| `PostCallEvent` | `◀ AGENT RESULT` | Agent 本轮最终消息，附 `GenerateReason` |
| `ErrorEvent` | `❌ ERROR` | 异常类型 + 错误信息 |

**特点**：

- 纯日志观察，**不修改任何事件或消息**（`Mono.just(event)`）。
- 带 ANSI 颜色（CYAN / GREEN / YELLOW / RED / MAGENTA）、分隔线、角色着色、文本截断（`truncate 200/150`）。
- `extractToolOutputText(ToolResultBlock, fallback)` 是 **static 工具方法**，`HitlInteractionExample.toolResultEvent` 复用它把工具输出提取成纯文本：

  ```java
  "toolResult", ObservationHook.extractToolOutputText(result, "")
  ```

---

### 3. `UserInteractionTool`（`ask_user`）

**位置**：`UserInteractionTool.java`

通过 `@Tool` 注解声明工具，工具名 `TOOL_NAME = "ask_user"`。

#### 入参（由 LLM 根据 prompt 决定）

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `question` | String | 是 | 要问用户的问题 |
| `ui_type` | String | 是 | UI 组件类型：`text` / `select` / `multi_select` / `confirm` / `form` / `date` / `number` |
| `options` | `List<String>` | 选填 | `select` / `multi_select` 的候选项 |
| `fields` | `List<Map<String,Object>>` | 选填 | `form` 时的字段定义（name/label/type/placeholder/required/options/min/max/step） |
| `default_value` | Object | 选填 | 默认值 |
| `allow_other` | Boolean | 选填 | `select` / `multi_select` 时是否允许"其他（自定义输入）" |

#### 实现核心

```java
throw new ToolSuspendException(reason);
```

**工具体不做任何业务**，直接抛 `ToolSuspendException`：

- AgentScope 框架捕获该异常后，将当前工具调用标记为**挂起（suspended）**，生成 `GenerateReason.TOOL_SUSPENDED` 的 `AGENT_RESULT`。
- `HitlInteractionExample.convertEvent` 的 `AGENT_RESULT` 分支识别到 `TOOL_SUSPENDED` 且工具名为 `ask_user`，发出 `USER_INTERACTION` 事件，前端据此渲染动态 UI。
- 用户填完后，前端调用 `POST /api/chat/respond`，把答案封装为 `ToolResultBlock` 回注，Agent 从挂起点恢复。

**为什么工具体为空**：`ask_user` 不是一个"执行"的工具，而是一个"挂起点"——实际的"答案"由用户在前端产生，而不是由后端代码计算。

---

### 4. `AddCalendarEventTool`（`add_calendar_event`）

**位置**：`AddCalendarEventTool.java`

标准的"写动作"工具，工具名 `TOOL_NAME = "add_calendar_event"`。

#### 入参

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `title` | String | 是 | 事件标题，如 "Chest + Triceps Workout" |
| `date` | String | 是 | 日期 `YYYY-MM-DD` |
| `time` | String | 选填 | 开始时间 `HH:mm`，默认 `09:00` |
| `duration_minutes` | Integer | 选填 | 时长（分钟），默认 60 |
| `description` | String | 选填 | 训练详情 |

#### 实现

当前示例为 **Mock 实现**：直接返回一条格式化字符串，不真正写日历。

```java
return String.format(
    "Successfully added calendar event: '%s' on %s at %s (%d min)",
    title, date, startTime, duration);
```

**为什么被纳入确认名单**：`add_calendar_event` 是"副作用"工具（会真实写入用户日历），按 HITL 最佳实践，**所有对外部系统有写入影响的工具都应强制人工确认**。系统提示词明确要求"每个训练日调用一次 `add_calendar_event`"，因此 Agent 会批量产生多次调用，`ToolConfirmationHook` 在真正执行前一次性弹确认框给用户审批。

---

## 六、各组件配合关系一览

```
┌───────────────────────────────────────────────────────────────────┐
│  HitlInteractionExample (Controller)                              │
│  ├─ /chat         → agent.stream(userMsg)                         │
│  ├─ /chat/respond → agent.stream(ToolResultBlock[ask_user])       │
│  ├─ /chat/confirm → agent.stream(...) 或 ToolResultBlock[拒绝]    │
│  ├─ /chat/interrupt → agent.interrupt()                           │
│  └─ /chat/session/{id} DELETE → session.delete(...)               │
└───────────────────────────┬───────────────────────────────────────┘
                            │ Event 流
                            ▼
┌───────────────────────────────────────────────────────────────────┐
│  ReActAgent (FitnessCoach)                                        │
│  ├─ model: DashScopeChatModel (qwen-max, stream)                  │
│  ├─ memory: InMemoryMemory                                        │
│  ├─ toolkit: [ask_user, add_calendar_event]                       │
│  └─ hooks:                                                        │
│      ├─ ToolConfirmationHook → 拦截 add_calendar_event，stopAgent │
│      └─ ObservationHook      → 全生命周期日志观察                 │
└───────────────────────────┬───────────────────────────────────────┘
                            │
            ┌───────────────┴────────────────┐
            ▼                                ▼
┌─────────────────────────┐    ┌─────────────────────────────────┐
│ UserInteractionTool     │    │ AddCalendarEventTool            │
│  ask_user → 抛          │    │  add_calendar_event → 返回      │
│  ToolSuspendException   │    │  "Successfully added ..."       │
│  触发 TOOL_SUSPENDED    │    │  （被 Hook 拦截，需先确认）     │
└─────────────────────────┘    └─────────────────────────────────┘
```
