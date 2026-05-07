package com.coderpwh.htilchat.controller;

import com.coderpwh.htilchat.dto.ChatEvent;
import com.coderpwh.htilchat.dto.ChatRequest;
import com.coderpwh.htilchat.service.AgentService;
import com.coderpwh.htilchat.service.McpService;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.rmi.ServerError;

/**
 * @author coderpwh
 */
@RestController
@RequestMapping("/api")
public class ChatController {


    private  final AgentService agentService;

    private final McpService mcpService;


    public ChatController(AgentService agentService, McpService mcpService) {
        this.agentService = agentService;
        this.mcpService = mcpService;
    }


    @PostMapping(value = "/chat",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<ChatEvent>> chat(@RequestBody ChatRequest request){

        String sessionId = request.getSessionId();

        if(sessionId==null||sessionId.isEmpty()){
            sessionId="defalut";
        }
        return  agentService.chat(sessionId,request.getMessage()).map(event-> ServerSentEvent.builder(event).build());
    }





}
