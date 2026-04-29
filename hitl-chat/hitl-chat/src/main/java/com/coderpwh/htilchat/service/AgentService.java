package com.coderpwh.htilchat.service;

import com.coderpwh.htilchat.dto.ChatEvent;
import com.coderpwh.htilchat.hook.ToolConfirmationHook;
import com.coderpwh.htilchat.tools.BuiltinTools;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.formatter.dashscope.DashScopeChatFormatter;
import io.agentscope.core.memory.InMemoryMemory;
import io.agentscope.core.message.ContentBlock;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.message.ToolResultBlock;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.session.InMemorySession;
import io.agentscope.core.session.Session;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.core.tool.file.ReadFileTool;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
