package com.coderpwh.chattts.controller;

import io.agentscope.core.hook.TTSHook;
import io.agentscope.core.message.Base64Source;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.model.tts.DashScopeRealtimeTTSModel;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

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


    @RequestMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<Map<String, Object>>> chat(@RequestBody Map<String, String> request) {
        String messages = request.get("message");

        if (messages == null || messages.isEmpty()) {
            return Flux.just(ServerSentEvent.<Map<String, Object>>builder().event("error").data(Map.of("error", "Message is required")).build());
        }


        Sinks.Many<ServerSentEvent<Map<String, Object>>> sink = Sinks.many().multicast().onBackpressureBuffer();

        DashScopeRealtimeTTSModel requestTtsModel = DashScopeRealtimeTTSModel.builder()
                .apiKey(apiKey)
                .modelName("qwen3-tts-flash-realtime")
                .voice("Cherry")
                .sampleRate(24000)
                .format("pcm")
                .build();


         //  创建TTSHook
        TTSHook  ttsHook = TTSHook.builder()
                .ttsModel(requestTtsModel)
                .audioCallback(audio->{
                   if(audio.getSource() instanceof Base64Source src ){
                       sink.tryEmitNext(
                               ServerSentEvent.<Map<String,Object>>builder()
                                       .event("audio")
                                       .data(Map.of("audio",src.getData())).build()
                       );
                   }
                }).build();






        return null;
    }


}
