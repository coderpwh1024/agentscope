package com.coderpwh.routing.graph.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.alibaba.cloud.ai.graph.agent.flow.node.RoutingMergeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Map;

public class PostprocessNode implements NodeAction {


    private static final Logger log = LoggerFactory.getLogger(PostprocessNode.class);

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        String mergedResult = state.value(RoutingMergeNode.DEFAULT_MERGED_OUTPUT_KEY)
                .map(Object::toString)
                .orElse("No result from routing.");

        @SuppressWarnings("unchecked")
        Map<String, Object> preprocessMeta = (Map<String, Object>) state.value("preprocess_metadata").orElse(Map.of());

        String traceId = (String) preprocessMeta.getOrDefault("tracedId", "unknown");

        String timestamp = Instant.now().toString();

        String formatted =
                String.format(
                        """
                                --- Answer (traceId=%s) ---
                                %s
                                ---
                                Generated at: %s
                                """,
                        traceId, mergedResult, timestamp);

        log.info("Postprocess: traceId={}, result length={}", traceId, mergedResult.length());

        return Map.of("final_answer", formatted, "postprocess_metadata", Map.of("traceId", traceId, "timestamp", timestamp, "resultLength", mergedResult.length()));
    }
}
