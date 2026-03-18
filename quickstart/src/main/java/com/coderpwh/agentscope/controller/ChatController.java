package com.coderpwh.agentscope.controller;

import com.coderpwh.agentscope.util.MsgUtils;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.agent.EventType;
import io.agentscope.core.agent.StreamOptions;
import io.agentscope.core.message.Msg;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.session.JsonSession;
import io.agentscope.core.session.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@RestController
@RequestMapping("/stream")
public class ChatController implements InitializingBean {
    private String apiKey;

    private Path sessionPath;

    @Override
    public void afterPropertiesSet() throws Exception {

        apiKey = "";
        if (apiKey == null || apiKey.isEmpty()) {
            System.out.println("请设置apiKey");
            throw new IllegalStateException("api是必填项");
        }

        sessionPath = Paths.get(System.getProperty("user.home"), ".agentscope", "examples", "web-sessions");
        System.out.println("\n=== StreamingWeb Example Started ===");
        System.out.println("Server running at: http://localhost:8080");
        System.out.println("\nTry:");
        System.out.println("  curl -N \"http://localhost:8080/chat?message=Hello\"");
        System.out.println(
                "  curl -N"
                        + " \"http://localhost:8080/chat?message=What%20is%20AI?&sessionId=my-session\"");
        System.out.println("\nPress Ctrl+C to stop.\n");
    }

    @GetMapping(path = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(@RequestParam String message, @RequestParam(defaultValue = "default") String sessionId) {

        ReActAgent agent = ReActAgent.builder()
                .name("Assistant")
                .model(DashScopeChatModel
                        .builder()
                        .apiKey(apiKey)
                        .modelName("qwen-plus")
                        .stream(true)
                        .build()).build();

        Session session = new JsonSession(sessionPath);
        agent.loadIfExists(session, sessionId);

        Msg userMsg = Msg.builder().textContent(message).build();

        StreamOptions streamOptions = StreamOptions
                .builder()
                .eventTypes(EventType.REASONING, EventType.TOOL_RESULT)
                .incremental(true)
                .includeReasoningResult(false)
                .build();

        return agent
                .stream(userMsg, streamOptions)
                .subscribeOn(Schedulers.boundedElastic())
                .doFinally(signalType -> {
                    agent.saveTo(session, sessionId);
                })
                .map(event -> {
                    return MsgUtils.getTextContent(event.getMessage());
                })
                .filter(text -> text != null && !text.isEmpty());
    }

    @GetMapping("/health")
    public String health() {
        return "OK";
    }
}
