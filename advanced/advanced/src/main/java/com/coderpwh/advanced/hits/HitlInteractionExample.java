package com.coderpwh.advanced.hits;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.agent.Event;
import io.agentscope.core.agent.StreamOptions;
import io.agentscope.core.formatter.dashscope.DashScopeChatFormatter;
import io.agentscope.core.memory.InMemoryMemory;
import io.agentscope.core.message.GenerateReason;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.message.ToolResultBlock;
import io.agentscope.core.message.ToolUseBlock;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.session.InMemorySession;
import io.agentscope.core.session.Session;
import io.agentscope.core.state.SimpleSessionKey;
import io.agentscope.core.tool.Toolkit;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;


/**
 * @author coderpwh
 */
@SpringBootApplication
@RestController
@RequestMapping("/api")
public class HitlInteractionExample {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();


    private static final Set<String> TOOLS_REQUIRING_CONFIRMATION =
            Set.of(AddCalendarEventTool.TOOL_NAME);

    private static final String SYS_PROMPT =
            """
                    你是一位专业的健身教练助手，负责制定个性化的训练计划。
                    规则：
                    - 绝不在普通文本中提问。必须始终使用 ask_user 工具。
                    - 每次响应只能调用一次 ask_user。
                    - 使用与用户输入相同的语言进行回复。
                    需要收集的信息（每次收集一项，跳过用户已提供的内容）：
                    - 健身目标：单选 — 减脂、增肌、综合健身、柔韧性训练
                    - 身体信息（年龄、身高、体重）：包含数字输入框的表单
                    - 可用器材：多选（允许自定义）— 例如哑铃、杠铃、跑步机、引体向上架、弹力带（根据用户目标进行调整）
                    - 每周训练天数：数字
                    - 受伤 / 健康问题：先确认是否有，若有则填写文本
                    - 计划开始日期：日期
                    工作流程：
                    1. 通过 ask_user 逐一收集缺失信息。
                    2. 生成详细的每周计划，包括训练动作、组数、次数和休息时间。
                    3. 每个训练日调用一次 add_calendar_event，将其添加到日历中。
                    """;


    private final Session session = new InMemorySession();

    private final ConcurrentHashMap<String, ReActAgent> runningAgents = new ConcurrentHashMap<>();

    private final Toolkit toolkit;

    private final DashScopeChatModel model;


    {
        String apiKey = System.getenv("DASHSCOPE_API_KEY");
        toolkit = new Toolkit();
        toolkit.registerTool(new UserInteractionTool());
        toolkit.registerTool(new AddCalendarEventTool());

        model =
                DashScopeChatModel.builder().apiKey(apiKey).modelName("qwen-max").stream(true)
                        .enableThinking(false)
                        .formatter(new DashScopeChatFormatter())
                        .build();
    }

    public static void main(String[] args) {
        String apiKey = System.getenv("DASHSCOPE_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("Error: DASHSCOPE_API_KEY environment variable not set.");
            System.err.println("Please set it with: export DASHSCOPE_API_KEY=your_api_key");
            System.exit(1);
        }

        System.out.println("\n" + "=".repeat(70));
        System.out.println("  HITL Interactive UI Example");
        System.out.println("  Agent with dynamic UI-based user interaction");
        System.out.println("=".repeat(70));
        System.out.println("  Open: http://localhost:8080/hitl-interaction/index.html");
        System.out.println("=".repeat(70) + "\n");

        SpringApplication.run(HitlInteractionExample.class, args);
    }


    @RequestMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<Map<String, Object>>> chat(@RequestBody Map<String, String> request) {

        String sessionId = (String) request.get("sessionId");

        String message = request.get("message");

        if (message == null || message.isEmpty()) {
            return Flux.just(ServerSentEvent.<Map<String, Object>>builder().data(errorEvent("Missing required parameter:message")).build());
        }

        ReActAgent agent = createAgent(sessionId);
        runningAgents.put(sessionId, agent);

        Msg userMsg = Msg.builder()
                .name("User")
                .role(MsgRole.USER)
                .content(TextBlock.builder().text(message).build())
                .build();

        Flux<Map<String, Object>> events = agent.stream(userMsg).flatMap(this::convertEvent);
        return wrapAsSSE(sessionId, agent, events);
    }


    private ReActAgent createAgent(String sessionId) {
        ReActAgent agent = ReActAgent.builder()
                .name("FitnessCoach")
                .sysPrompt(SYS_PROMPT)
                .model(model)
                .toolkit(toolkit)
                .memory(new InMemoryMemory())
                .hook(new ToolConfirmationHook(TOOLS_REQUIRING_CONFIRMATION))
                .hook(new ObservationHook())
                .build();

        agent.loadIfExists(session, sessionId);
        return agent;
    }


    /***
     *
     * @param sessionId
     * @param agent
     * @param events
     * @return
     */

    private Flux<ServerSentEvent<Map<String, Object>>> wrapAsSSE(String sessionId, ReActAgent agent, Flux<Map<String, Object>> events) {
        return events.concatWith(
                        Flux.just(completeEvent()))
                .onErrorResume(error -> Flux.just(errorEvent(error.getMessage()), completeEvent()))
                .doFinally(
                        signal -> {
                            runningAgents.remove(sessionId);
                            agent.saveTo(session, sessionId);
                        }).map(data -> ServerSentEvent.<Map<String, Object>>builder().data(data).build());
    }

    private Flux<Map<String, Object>> convertEvent(Event event) {
        List<Map<String, Object>> events = new ArrayList<>();
        Msg msg = event.getMessage();

        switch (event.getType()) {
            case REASONING -> {
                if (event.isLast() && msg.hasContentBlocks(ToolUseBlock.class)) {
                    List<ToolUseBlock> toolCalls = msg.getContentBlocks(ToolUseBlock.class);
                    boolean needsConfirm =
                            toolCalls.stream()
                                    .anyMatch(
                                            t ->
                                                    TOOLS_REQUIRING_CONFIRMATION.contains(
                                                            t.getName()));

                    if (needsConfirm) {
                        // Tools require user approval — emit TOOL_CONFIRM
                        events.add(toolConfirmEvent(toolCalls));
                    } else {
                        // Normal tool calls — show non-ask_user tools
                        for (ToolUseBlock tool : toolCalls) {
                            if (!UserInteractionTool.TOOL_NAME.equals(tool.getName())) {
                                events.add(toolUseEvent(tool));
                            }
                        }
                    }
                } else {
                    // Streaming text chunks
                    String text = extractText(msg);
                    if (text != null && !text.isEmpty()) {
                        events.add(textEvent(text, !event.isLast()));
                    }
                }
            }
            case TOOL_RESULT -> {
                for (ToolResultBlock result : msg.getContentBlocks(ToolResultBlock.class)) {
                    if (!UserInteractionTool.TOOL_NAME.equals(result.getName())) {
                        events.add(toolResultEvent(result));
                    }
                }
            }
            case AGENT_RESULT -> {
                GenerateReason reason = msg.getGenerateReason();
                if (reason == GenerateReason.TOOL_SUSPENDED) {
                    List<ToolUseBlock> toolCalls = msg.getContentBlocks(ToolUseBlock.class);
                    for (ToolUseBlock tool : toolCalls) {
                        if (UserInteractionTool.TOOL_NAME.equals(tool.getName())) {
                            events.add(userInteractionEvent(tool));
                        }
                    }
                }
            }
            default -> {
                // HINT, SUMMARY, etc. - ignore for simplicity
            }
        }

        return Flux.fromIterable(events);
    }


    private Map<String, Object> textEvent(String content, boolean incremental) {
        return Map.of("type", "TEXT", "content", content, "incremental", incremental);
    }

    private Map<String, Object> toolUseEvent(ToolUseBlock tool) {
        return Map.of(
                "type", "TOOL_USE",
                "toolId", tool.getId(),
                "toolName", tool.getName(),
                "toolInput", convertInput(tool.getInput()));
    }

    private Map<String, Object> toolResultEvent(ToolResultBlock result) {
        return Map.of(
                "type", "TOOL_RESULT",
                "toolId", result.getId(),
                "toolName", result.getName(),
                "toolResult", ObservationHook.extractToolOutputText(result, ""));
    }

    /**
     * Build a TOOL_CONFIRM event containing all pending tool calls that need user approval.
     */
    private Map<String, Object> toolConfirmEvent(List<ToolUseBlock> toolCalls) {
        List<Map<String, Object>> pending =
                toolCalls.stream()
                        .map(
                                tool ->
                                        Map.<String, Object>of(
                                                "id", tool.getId(),
                                                "name", tool.getName(),
                                                "input", convertInput(tool.getInput()),
                                                "needsConfirm",
                                                TOOLS_REQUIRING_CONFIRMATION.contains(
                                                        tool.getName())))
                        .toList();
        return Map.of("type", "TOOL_CONFIRM", "pendingToolCalls", pending);
    }

    /**
     * Build a USER_INTERACTION event from the ask_user tool's ToolUseBlock.
     *
     * <p>The tool's input parameters contain the UI specification:
     * question, ui_type, options, fields, default_value.
     */
    private Map<String, Object> userInteractionEvent(ToolUseBlock tool) {
        Map<String, Object> input = tool.getInput();
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("type", "USER_INTERACTION");
        event.put("toolId", tool.getId());
        event.put("question", input.getOrDefault("question", "Please provide more information"));
        event.put("uiType", input.getOrDefault("ui_type", "text"));

        if (input.containsKey("options")) {
            event.put("options", input.get("options"));
        }
        if (input.containsKey("fields")) {
            event.put("fields", input.get("fields"));
        }
        if (input.containsKey("default_value")) {
            event.put("defaultValue", input.get("default_value"));
        }
        if (Boolean.TRUE.equals(input.get("allow_other"))) {
            event.put("allowOther", true);
        }

        return event;
    }

    private static Map<String, Object> completeEvent() {
        return Map.of("type", "COMPLETE");
    }

    private static Map<String, Object> errorEvent(String error) {
        return Map.of("type", "ERROR", "error", error != null ? error : "Unknown error");
    }

    // ==================== Helpers ====================

    private String extractText(Msg msg) {
        List<TextBlock> textBlocks = msg.getContentBlocks(TextBlock.class);
        if (textBlocks.isEmpty()) {
            return null;
        }
        return textBlocks.stream().map(TextBlock::getText).collect(Collectors.joining());
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> convertInput(Object input) {
        if (input == null) {
            return Map.of();
        }
        if (input instanceof Map) {
            return (Map<String, Object>) input;
        }
        try {
            return OBJECT_MAPPER.convertValue(input, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            return Map.of("value", input.toString());
        }
    }

}
