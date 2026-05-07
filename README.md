# AgentScope Java Examples 学习路线

> 基于 [agentscope-ai/agentscope-java](https://github.com/agentscope-ai/agentscope-java/tree/main/agentscope-examples) 仓库，按"能力依赖关系 + 由浅入深"整理的学习/实现顺序。

## 目录

- [进度概览](#进度概览)
- [阶段 1:核心能力补完](#阶段-1核心能力补完)
- [阶段 2:协议与集成](#阶段-2协议与集成)
- [阶段 3:游戏化多 Agent](#阶段-3游戏化多-agent)
- [阶段 4:框架集成与性能](#阶段-4框架集成与性能)
- [阶段 5:终极综合 Demo](#阶段-5终极综合-demo)
- [学习路径图](#学习路径图)
- [推荐主线](#推荐主线)

---

## 进度概览

### ✅ 已完成(4 个)

| Example | 说明 |
|---|---|
| `quickstart` | 入门,ReActAgent 基本用法、Session 持久化等 |
| `advanced` | 进阶特性 |
| `chat-tts` | Qwen TTS 模型集成 |
| `hitl-chat` | Human-in-the-Loop + MCP 工具确认 |

### ⏳ 待完成(13 个)

a2a-rocketmq、a2a、agui、boba-tea-shop、chat-completions-web、graceful-shutdown、micronaut、model-request-compression、multiagent-patterns、plan-notebook、quarkus、werewolf-hitl、werewolf

---

## 阶段 1:核心能力补完

> 单 Agent 能力的剩余拼图,为后续多 Agent 场景打基础。

### 1. multiagent-patterns ⭐ 优先做

- 多 Agent 协作的"模式手册"(Sequential、Parallel、Router、Handoff 等 Pipeline / MsgHub 用法)
- 这是后面所有多 Agent example 的基础,必须先打通
- 对应 Python 版的 pipeline 概念

### 2. plan-notebook

- AgentScope 的招牌特性之一:结构化任务分解
- 单 Agent 也能用,难度低,价值高
- 学完后对"长任务规划"的实现模式就有了认识

### 3. graceful-shutdown

- 学习 Safe Interruption / Graceful Cancellation
- 偏运行时控制,代码量小,是生产级特性的入门

---

## 阶段 2:协议与集成

> 让 Agent 能对外通信、对接前端、与其他 Agent 协作。

### 4. chat-completions-web

- 把 Agent 包装成 OpenAI Chat Completions 兼容接口
- 对接现有客户端(Cherry Studio、各种 OpenAI SDK)非常实用
- 难度低,效果立竿见影

### 5. agui

- AG-UI 协议(前端流式渲染标准),Spring Boot starter 做集成
- 学完可以给 Agent 做一个像样的 Web UI(CopilotKit 等)
- 是"Agent + 前端"的标准做法

### 6. a2a

- Agent2Agent 协议 + Nacos 注册发现
- 进入"分布式多 Agent"领域的入口
- 需要本地起 Nacos,环境稍重

### 7. a2a-rocketmq

- a2a 的消息队列变体(异步通信)
- 必须先做完 a2a 再做这个

---

## 阶段 3:游戏化多 Agent

> 综合演练,把前面学到的多 Agent 能力跑起来。

### 8. werewolf

- 狼人杀,经典多 Agent 协作 demo
- 是 multiagent-patterns 的实战版,所有 Pipeline / MsgHub / 角色扮演技巧的集大成
- 对理解 Agent 间博弈非常有帮助

### 9. werewolf-hitl

- 在 werewolf 基础上加 Human-in-the-Loop
- 必须先做 werewolf
- 之前做过 hitl-chat,对 HITL 机制不陌生,组合起来即可

---

## 阶段 4:框架集成与性能

> 按需选做。如果日常只用 Spring Boot,这一阶段可以略读不实操。

### 10. micronaut

- AgentScope 在 Micronaut 框架下的集成

### 11. quarkus

- AgentScope 在 Quarkus 框架下的集成
- 涉及 GraalVM native image,能体验 200ms 冷启动

### 12. model-request-compression

- 用 brotli 压缩模型请求体,省带宽
- 偏优化技巧,独立小特性,最后做

---

## 阶段 5:终极综合 Demo

### 13. boba-tea-shop ⭐ 收官项目

- AgentScope Java 的"旗舰 demo",奶茶店多 Agent 系统
- 涉及:Supervisor + Sub Agents、RAG、长期记忆、MCP Server、Nacos、Spring Boot 多模块
- **必须放最后**:它需要前面几乎所有 example 的知识(multiagent-patterns、a2a、MCP、agui 都会用到)
- 做完这个就基本掌握 AgentScope Java 全貌了

**能力清单:**

- 奶茶推荐:基于 RAG 知识库 + 用户偏好分析
- 智能点单:自然语言识别商品、甜度、冰量
- 多 Agent 服务层:Supervisor Agent(店长) + Business Sub Agent(店员)
- MCP Server:处理具体业务逻辑
- Nacos:Agent 与 MCP 的动态注册和发现
- 数据持久化:知识库、会话、记忆、业务数据

---

## 学习路径图

```
quickstart ✓
advanced ✓        ┐
chat-tts ✓        ├─ 已完成
hitl-chat ✓       ┘
       ↓
[1] multiagent-patterns ─┬→ [8] werewolf ─→ [9] werewolf-hitl
       ↓                  │
[2] plan-notebook         │
[3] graceful-shutdown     │
       ↓                  │
[4] chat-completions-web  │
[5] agui                  │
[6] a2a ─→ [7] a2a-rocketmq
       ↓                  │
[10] micronaut (可选)      │
[11] quarkus  (可选)       │
[12] model-request-compression (可选)
       ↓                  │
[13] boba-tea-shop ←──────┘  (终极综合)
```

---

## 推荐主线

根据不同目标,可以选择不同路径:

### 时间紧 / 快速覆盖核心

```
1 → 2 → 5 → 6 → 13
```

multiagent-patterns → plan-notebook → agui → a2a → boba-tea-shop,覆盖 80% 核心能力。

### 写技术分享 / 博客

重点关注 **werewolf** 和 **boba-tea-shop**,这两个最有"展示价值"。

### 项目要落地生产

优先深入工程化特性:

- `graceful-shutdown` — 运行时控制
- `agui` — 前端集成
- `a2a` — 分布式 Agent
- `model-request-compression` — 性能优化

游戏 demo(werewolf)反而不是重点。

---

## 参考链接

- 主仓库: <https://github.com/agentscope-ai/agentscope-java>
- Examples 目录: <https://github.com/agentscope-ai/agentscope-java/tree/main/agentscope-examples>
- 官方文档: <https://java.agentscope.io>
