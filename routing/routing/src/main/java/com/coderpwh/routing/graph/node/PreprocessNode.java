package com.coderpwh.routing.graph.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.UserMessage;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;

public class PreprocessNode implements NodeAction {


    private static final Logger log = LoggerFactory.getLogger(PreprocessNode.class);

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        String input = state.value("input").map(Object::toString)
                .orElse(state.value("query").map(Object::toString).orElse(""));

        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("Query cannot be empty");
        }

        if (input.length() < 3) {
            throw new IllegalArgumentException("Query too short for meaningful routing");
        }

        String enrichedQuery = input.trim();
        if (enrichedQuery.length() > 2000) {
            enrichedQuery = enrichedQuery.substring(0, 2000) + "...";
            log.info("只能查询2000个字符");
        }

        String traceId = UUID.randomUUID().toString();
        String timestamp = Instant.now().toString();

        List<Message> messages = List.of(new UserMessage(enrichedQuery));

        return Map.of("input", enrichedQuery,
                "messages", messages,
                "preprocess_metadata",
                Map.of("tracedId", traceId, "timestamp", timestamp, "originalLength", input.length())
        );

    }
}
