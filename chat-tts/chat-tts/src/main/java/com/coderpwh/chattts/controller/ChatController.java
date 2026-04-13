package com.coderpwh.chattts.controller;

import io.agentscope.core.model.DashScopeChatModel;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.Map;

/**
 * @author coderpwh
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ChatController {

    private final DashScopeChatModel chatModel;

    private final String apiKey;


    public ChatController() {
        String apiKey = System.getenv("DASHSCOPE_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("DASHSCOPE_API_KEY environment variable is required");
        }

        this.apiKey = apiKey;
        this.chatModel = DashScopeChatModel.builder().apiKey(apiKey).modelName("qwen-plus").build();
    }


    public Flux<ServerSentEvent<Map<String,Object>>> chat(){
        return null;
    }




}
