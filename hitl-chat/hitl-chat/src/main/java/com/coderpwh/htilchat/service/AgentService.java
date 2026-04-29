package com.coderpwh.htilchat.service;

import com.coderpwh.htilchat.hook.ToolConfirmationHook;
import com.coderpwh.htilchat.tools.BuiltinTools;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.session.InMemorySession;
import io.agentscope.core.session.Session;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.core.tool.file.ReadFileTool;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashSet;
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






}
