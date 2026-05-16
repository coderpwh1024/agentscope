package com.coderpwh.routing.simple;

import com.alibaba.cloud.ai.agent.agentscope.flow.AgentScopeRoutingAgent;
import com.coderpwh.routing.simple.state.AgentOutput;
import com.coderpwh.routing.simple.state.Classification;
import io.agentscope.core.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.Message;

import java.util.List;

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






   private static  String extraText(Object output){
        if(output instanceof Message message){
            return  message.getText();
        }

        return  output!=null?output.toString():"";
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
