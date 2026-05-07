package com.coderpwh.htilchat.service;

import com.coderpwh.htilchat.dto.ChatEvent;
import com.coderpwh.htilchat.dto.ToolConfirmRequest;
import com.coderpwh.htilchat.hook.ToolConfirmationHook;
import com.coderpwh.htilchat.tools.BuiltinTools;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.agent.Event;
import io.agentscope.core.agent.StreamOptions;
import io.agentscope.core.formatter.dashscope.DashScopeChatFormatter;
import io.agentscope.core.memory.InMemoryMemory;
import io.agentscope.core.message.ContentBlock;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.message.ToolResultBlock;
import io.agentscope.core.message.ToolUseBlock;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.session.InMemorySession;
import io.agentscope.core.session.Session;
import io.agentscope.core.state.SessionKey;
import io.agentscope.core.state.SimpleSessionKey;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.core.tool.file.ReadFileTool;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author coderpwh
 */
@Service
public class AgentService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();


    @Value("${dashscope.api-key:${DASHSCOPE_API_KEY:}}")
    private String apiKey;

    @Value("${dashscope.model-name:qwen-plus}")
    private String modelName;


    private final McpService mcpService;
    private Toolkit sharedToolkit;
    private ToolConfirmationHook confirmationHook;

    private final Session session = new InMemorySession();

    private final ConcurrentHashMap<String, ReActAgent> runningAgents = new ConcurrentHashMap<>();


    public AgentService(McpService mcpService) {
        this.mcpService = mcpService;
    }

    @PostConstruct
    public void init() {
        sharedToolkit = new Toolkit();
        sharedToolkit.registerTool(new BuiltinTools());
        sharedToolkit.registerTool(new ReadFileTool());
        Set<String> defaultDangerousTools = new HashSet<>();
        defaultDangerousTools.add("view_text_file");
        defaultDangerousTools.add("list_directory");
        confirmationHook = new ToolConfirmationHook(defaultDangerousTools);
    }

    public Toolkit getToolkit() {
        return sharedToolkit;
    }

    public Set<String> getToolNames() {
        return sharedToolkit.getToolNames();
    }

    public ToolConfirmationHook getConfirmationHook() {
        return confirmationHook;
    }


    /***
     *  创建智能体
     * @param sessionId
     * @return
     */
    private ReActAgent createAgent(String sessionId) {
        Toolkit sessionToolkit = sharedToolkit.copy();

        ReActAgent agent = ReActAgent.builder()
                .name("Assistant")
                .sysPrompt(
                        "你是一个智能助手，能够调用多种工具。"
                                + "请在合适的场景下使用这些工具来协助用户完成任务。")
                .model(
                        DashScopeChatModel.builder()
                                .apiKey(apiKey)
                                .modelName(modelName)
                                .stream(true)
                                .enableThinking(false)
                                .formatter(new DashScopeChatFormatter())
                                .build())
                .toolkit(sessionToolkit)
                .memory(new InMemoryMemory())
                .hook(confirmationHook)
                .build();
        agent.loadIfExists(session, sessionId);
        return agent;
    }

    public Flux<ChatEvent> chat(String sessionId, String message) {

        ReActAgent agent = createAgent(sessionId);

        runningAgents.put(sessionId, agent);

        Msg userMsg = Msg.builder()
                .name("User")
                .role(MsgRole.USER)
                .content(TextBlock.builder().text(message).build())
                .build();

        return agent.stream(userMsg)
                .flatMap(this::convertEventToChatEvents)
                .concatWith(Flux.just(ChatEvent.complete()))
                .doFinally(
                        signal -> {
                            runningAgents.remove(sessionId);
                            agent.saveTo(session, sessionId);
                        })
                .onErrorResume(
                        error ->
                                Flux.just(
                                        ChatEvent.error(error.getMessage()), ChatEvent.complete()));

    }

    /**
     * Confirm or reject pending tool execution.
     */
    public Flux<ChatEvent> confirmTool(
            String sessionId, boolean confirmed, String reason, List<ToolConfirmRequest.ToolCallInfo> toolCalls) {
        ReActAgent agent = createAgent(sessionId);
        runningAgents.put(sessionId, agent);

        if (confirmed) {
            return agent.stream(StreamOptions.defaults())
                    .flatMap(this::convertEventToChatEvents)
                    .concatWith(Flux.just(ChatEvent.complete()))
                    .doFinally(
                            signal -> {
                                runningAgents.remove(sessionId);
                                agent.saveTo(session, sessionId);
                            })
                    .onErrorResume(
                            error ->
                                    Flux.just(
                                            ChatEvent.error(error.getMessage()),
                                            ChatEvent.complete()));
        } else {
            List<ToolResultBlock> results = new ArrayList<>();
            String cancelMessage = reason != null ? reason : "Operation cancelled by user";
            if (toolCalls != null) {
                for (ToolConfirmRequest.ToolCallInfo tool : toolCalls) {
                    results.add(
                            ToolResultBlock.of(
                                    tool.getId(),
                                    tool.getName(),
                                    TextBlock.builder().text(cancelMessage).build()));
                }
            }
            Msg cancelResult =
                    Msg.builder()
                            .name("Assistant")
                            .role(MsgRole.TOOL)
                            .content(results.toArray(new ToolResultBlock[0]))
                            .build();

            return agent.stream(cancelResult)
                    .flatMap(this::convertEventToChatEvents)
                    .concatWith(Flux.just(ChatEvent.complete()))
                    .doFinally(
                            signal -> {
                                runningAgents.remove(sessionId);
                                agent.saveTo(session, sessionId);
                            })
                    .onErrorResume(
                            error ->
                                    Flux.just(
                                            ChatEvent.error(error.getMessage()),
                                            ChatEvent.complete()));
        }
    }

    /**
     * Clear a session.
     */
    public void clearSession(String sessionId) {
        session.delete(SimpleSessionKey.of(sessionId));
    }

    /**
     * Interrupt a running agent by sessionId.
     *
     * @param sessionId the session ID
     * @return true if an agent was found and interrupted, false otherwise
     */
    public boolean interrupt(String sessionId) {
        ReActAgent agent = runningAgents.get(sessionId);
        if (agent != null) {
            agent.interrupt();
            return true;
        }
        return false;
    }

    /**
     * Check if a session exists.
     */
    public boolean sessionExists(String sessionId) {
        return session.exists(SimpleSessionKey.of(sessionId));
    }

    /**
     * Get all session keys.
     */
    public Set<SessionKey> listSessionKeys() {
        return session.listSessionKeys();
    }


    /**
     * 转换事件
     * @param event
     * @return
     */
    private Flux<ChatEvent> convertEventToChatEvents(Event event) {
        List<ChatEvent> events = new ArrayList<>();
        Msg msg = event.getMessage();
        switch (event.getType()) {
            case REASONING -> {
                if (event.isLast() && msg.hasContentBlocks(ToolUseBlock.class)) {
                    List<ToolUseBlock> toolCalls = msg.getContentBlocks(ToolUseBlock.class);
                    boolean hasDangerous =
                            toolCalls.stream()
                                    .anyMatch(t -> confirmationHook.isDangerous(t.getName()));
                    if (hasDangerous) {
                        List<ChatEvent.PendingToolCall> pending = new ArrayList<>();
                        for (ToolUseBlock tool : toolCalls) {
                            pending.add(
                                    new ChatEvent.PendingToolCall(
                                            tool.getId(),
                                            tool.getName(),
                                            convertInput(tool.getInput()),
                                            confirmationHook.isDangerous(tool.getName())));
                        }
                        events.add(ChatEvent.toolConfirm(pending));
                    } else {
                        for (ToolUseBlock tool : toolCalls) {
                            events.add(
                                    ChatEvent.toolUse(
                                            tool.getId(),
                                            tool.getName(),
                                            convertInput(tool.getInput())));
                        }
                    }
                } else {
                    String text = extractText(msg);
                    if (text != null && !text.isEmpty()) {
                        events.add(ChatEvent.text(text, !event.isLast()));
                    }
                }
            }
            case TOOL_RESULT -> {
                for (ToolResultBlock result : msg.getContentBlocks(ToolResultBlock.class)) {
                    events.add(
                            ChatEvent.toolResult(
                                    result.getId(), result.getName(), extractToolOutput(result)));
                }
            }
            case AGENT_RESULT -> {
                String text = msg.getTextContent();
                if (text != null && !text.isEmpty()) {
                    events.add(ChatEvent.text(text, false));
                }
            }
            default -> {
            }
        }

        return Flux.fromIterable(events);
    }


    /***
     *  额外处理文本
     * @param msg
     * @return
     */
    private String extractText(Msg msg) {
        List<TextBlock> textBlocks = msg.getContentBlocks(TextBlock.class);

        if (textBlocks.isEmpty()) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (TextBlock tb : textBlocks) {
            sb.append(tb.getText());
        }
        return sb.toString();
    }


    /***
     *  额外处理工具输出
     * @param result
     * @return
     */
    private String extractToolOutput(ToolResultBlock result) {
        List<ContentBlock> outputs = result.getOutput();

        if (outputs == null || outputs.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (ContentBlock block : outputs) {
            if (block instanceof TextBlock tb) {
                sb.append(tb.getText());
            }
        }
        return sb.toString();
    }


    /**
     * 转换
     *
     * @param input
     * @return
     */
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
