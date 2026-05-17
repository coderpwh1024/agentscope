package com.coderpwh.routing.simple;

import com.alibaba.cloud.ai.agent.agentscope.flow.AgentScopeRoutingAgent;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.agent.flow.node.RoutingMergeNode;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.coderpwh.routing.simple.state.AgentOutput;
import com.coderpwh.routing.simple.state.Classification;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.model.ChatResponse;
import io.agentscope.core.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.Message;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


/**
 * @author coderpwh
 */
public class RouterService {

    private static final Logger log = LoggerFactory.getLogger(RouterService.class);

    private static final String[] OUTPUT_KEYS = {"github_key", "notion_key", "slack_key"};

    private static final String SYNTHESIZE_SYSTEM_TEMPLATE =
            """
                    Synthesize these search results to answer the original question: "%s"
                                        
                    - Combine information from multiple sources without redundancy
                    - Highlight the most relevant and actionable information
                    - Note any discrepancies between sources
                    - Keep the response concise and well-organized
                    """;

    private final Model model;

    private final AgentScopeRoutingAgent routingAgent;


    public RouterService(Model model, AgentScopeRoutingAgent routingAgent) {
        this.model = model;
        this.routingAgent = routingAgent;
    }


    public RouteResult run(String query) throws GraphRunnerException {
        Optional<OverAllState> resultOpt = routingAgent.invoke(query);
        if (resultOpt.isEmpty()) {
            return new RouteResult(query, List.of(), List.of(), "No result from router");
        }

        OverAllState state = resultOpt.get();
        String finalAnswer;

        List<Classification> classifications = collectClassifications(state);
        List<AgentOutput> results = collectAgentOutputs(state);
        log.info("Routed to {} sources: {}", classifications.size(), classifications);

        Optional<Object> mergedOpt = state.value(RoutingMergeNode.DEFAULT_MERGED_OUTPUT_KEY);
        if (mergedOpt.isPresent()) {
            finalAnswer = extractText(mergedOpt.get());
        } else {
            finalAnswer = synthesize(query, results);
        }
        return new RouteResult(query, classifications, results, finalAnswer);

    }



    /***
     *
     * @param state
     * @return
     */
    private List<Classification> collectClassifications(OverAllState state) {
        List<Classification> list = new ArrayList<>();
        for (String outputKey : OUTPUT_KEYS) {
            Optional<Object> outputOpt = state.value(outputKey);
            if (outputOpt.isPresent()) {
                String agentName = outputKey.replace("_key", "");
                String query = state.value(agentName + "_input").map(Object::toString).orElse("");
                list.add(new Classification(agentName, query));
            }
        }
        return list;
    }


    /**
     * 智能体集合输出
     *
     * @param state
     * @return
     */
    private List<AgentOutput> collectAgentOutputs(OverAllState state) {
        List<AgentOutput> list = new ArrayList<>();
        for (String outputKey : OUTPUT_KEYS) {
            Optional<Object> outputOpt = state.value(outputKey);
            if (outputOpt.isPresent()) {
                String agentName = outputKey.replace("_key", "");
                String result = RoutingMergeNode.extractText(outputOpt.get(), outputKey);
                list.add(new AgentOutput(agentName, result));
            }
        }
        return list;
    }


    /**
     * 同步
     *
     * @param query
     * @param results
     * @return
     */
    public String synthesize(String query, List<AgentOutput> results) {

        if (results == null || results.isEmpty()) {
            return "No results found from any knowledge source.";
        }
        String formatted =
                results.stream()
                        .map(r -> "**From " + capitalize(r.source()) + ":**\n" + r.result())
                        .reduce((a, b) -> a + "\n\n" + b)
                        .orElse("");

        String systemPrompt = SYNTHESIZE_SYSTEM_TEMPLATE.formatted(query);

        List<Msg> messages =
                List.of(
                        Msg.builder()
                                .role(MsgRole.SYSTEM)
                                .content(TextBlock.builder().text(systemPrompt).build())
                                .build(),
                        Msg.builder()
                                .role(MsgRole.USER)
                                .content(TextBlock.builder().text(formatted).build())
                                .build());


        Flux<ChatResponse> stream = model.stream(messages, null, null);

        ChatResponse last = stream.blockLast();
        if (last == null || last.getContent() == null) {
            return "";
        }

        StringBuilder text = new StringBuilder();
        for (var block : last.getContent()) {
            if (block instanceof TextBlock tb) {
                text.append(tb.getText());
            }
        }
        return text.toString();
    }


    private static String extractText(Object output) {
        if (output instanceof Message message) {
            return message.getText();
        }
        return output != null ? output.toString() : "";
    }



    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }


    /**
     * 路由返回结果
     *
     * @param query
     * @param classifications
     * @param results
     * @param finalAnswer
     */

    public record RouteResult(String query, List<Classification> classifications, List<AgentOutput> results,
                              String finalAnswer) {

    }


}
