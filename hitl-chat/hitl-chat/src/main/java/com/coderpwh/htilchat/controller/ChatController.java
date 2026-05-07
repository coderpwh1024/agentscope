package com.coderpwh.htilchat.controller;

import com.coderpwh.htilchat.dto.ChatEvent;
import com.coderpwh.htilchat.dto.ChatRequest;
import com.coderpwh.htilchat.dto.ToolConfirmRequest;
import com.coderpwh.htilchat.service.AgentService;
import com.coderpwh.htilchat.service.McpService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author coderpwh
 */
@RestController
@RequestMapping("/api")
public class ChatController {


    private final AgentService agentService;

    private final McpService mcpService;


    public ChatController(AgentService agentService, McpService mcpService) {
        this.agentService = agentService;
        this.mcpService = mcpService;
    }


    /***
     * 聊天
     * @param request
     * @return
     */
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<ChatEvent>> chat(@RequestBody ChatRequest request) {

        String sessionId = request.getSessionId();

        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = "default";
        }
        return agentService.chat(sessionId, request.getMessage()).map(event -> ServerSentEvent.builder(event).build());
    }


    /***
     * 确认工具
     * @param request
     * @return
     */

    @PostMapping(value = "/chat/confirm", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<ChatEvent>> confirmTool(@RequestBody ToolConfirmRequest request) {
        String sessionId = request.getSessionId();

        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = "default";
        }

        return agentService.confirmTool(sessionId, request.isConfirmed(), request.getReason(), request.getToolCalls())
                .map(event -> ServerSentEvent.builder(event).build());
    }


    /**
     * 清除会话
     *
     * @param sessionId
     * @return
     */
    @DeleteMapping("/chat/session/{sessionId}")
    public ResponseEntity<Map<String, Object>> clearSession(@PathVariable String sessionId) {
        agentService.clearSession(sessionId);
        return ResponseEntity.ok(Map.of("success", true));
    }


    /**
     * 中断会话
     *
     * @param sessionId
     * @return
     */

    @PostMapping("/chat/interrupt/{sessionId}")
    public ResponseEntity<Map<String, Object>> interrupt(@PathVariable String sessionId) {
        boolean interrupted = agentService.interrupt(sessionId);
        return ResponseEntity.ok(Map.of("success", true, "interrupted", interrupted));
    }


    /**
     * mcp 列表
     *
     * @return
     */
    @GetMapping("/mcp/list")
    public ResponseEntity<List<String>> listMcpService() {
        return ResponseEntity.ok(mcpService.listMcpServers());
    }


    /***
     * 工具
     * @return
     */
    @GetMapping("/tools")
    public ResponseEntity<Set<String>> getTools(){
        return  ResponseEntity.ok(agentService.getToolNames());
    }


    /**
     * 危险工具
     * @return
     */
    @GetMapping("/settings/dangerous-tools")
    public ResponseEntity<Set<String>> getDangerousTools() {
        return ResponseEntity.ok(agentService.getConfirmationHook().getDangerousTools());
    }

    /***
     * 设置危险工具
     * @param toolNames
     * @return
     */
    @PostMapping("/settings/dangerous-tools")
    public ResponseEntity<Map<String, Object>> setDangerousTools(
            @RequestBody Set<String> toolNames) {
        agentService.getConfirmationHook().setDangerousTools(toolNames);
        return ResponseEntity.ok(Map.of("success", true));
    }







    }
