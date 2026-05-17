//package com.coderpwh.routing.graph;
//
//import com.alibaba.cloud.ai.agent.agentscope.AgentScopeAgent;
//import com.alibaba.cloud.ai.agent.agentscope.flow.AgentScopeRoutingAgent;
//import com.alibaba.cloud.ai.graph.CompiledGraph;
//import com.alibaba.cloud.ai.graph.KeyStrategy;
//import com.alibaba.cloud.ai.graph.KeyStrategyFactory;
//import com.alibaba.cloud.ai.graph.StateGraph;
//import com.alibaba.cloud.ai.graph.exception.GraphStateException;
//import com.alibaba.cloud.ai.graph.state.strategy.AppendStrategy;
//import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
//import com.coderpwh.routing.graph.node.PostprocessNode;
//import com.coderpwh.routing.graph.node.PreprocessNode;
//import com.coderpwh.routing.graph.service.RoutingGraphService;
//import com.coderpwh.routing.graph.tools.GitHubStubTools;
//import com.coderpwh.routing.graph.tools.NotionStubTools;
//import com.coderpwh.routing.graph.tools.SlackStubTools;
//import io.agentscope.core.ReActAgent;
//import io.agentscope.core.memory.InMemoryMemory;
//import io.agentscope.core.model.DashScopeChatModel;
//import io.agentscope.core.model.Model;
//import io.agentscope.core.tool.Toolkit;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import static com.alibaba.cloud.ai.graph.StateGraph.END;
//import static com.alibaba.cloud.ai.graph.StateGraph.START;
//import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;
//
//@Configuration
//public class RoutingGraphConfig {
//
//    private static final String GITHUB_PROMPT =
//            """
//                    You are a GitHub expert. Answer questions about code, API references, and implementation \
//                    details by searching repositories, issues, and pull requests.
//                    Please respond to the following request:  {input}
//                    """;
//
//    private static final String NOTION_PROMPT =
//            """
//                    You are a Notion expert. Answer questions about internal processes, policies, and team \
//                    documentation by searching the organization's Notion workspace.
//                    Please respond to the following request: {input}
//                    """;
//
//    private static final String SLACK_PROMPT =
//            """
//                    You are a Slack expert. Answer questions by searching relevant threads and discussions \
//                    where team members have shared knowledge and solutions.
//                    Please respond to the following request: {input}
//                    """;
//
//    private static Model dashScopeModel() {
//        String key = System.getenv("AI_DASHSCOPE_API_KEY");
//        return DashScopeChatModel.builder().apiKey(key).modelName("qwen-plus").build();
//    }
//
//
//    /***
//     * github
//     * @return
//     */
//
//    @Bean
//    public AgentScopeAgent githubAgent(GitHubStubTools githubStubTools) {
//        Toolkit toolkit = new Toolkit();
//        toolkit.registerTool(githubStubTools);
//        ReActAgent.Builder builder =
//                ReActAgent.builder()
//                        .name("github")
//                        .description("GitHub specialist for code, issues, and PRs")
//                        .sysPrompt(GITHUB_PROMPT)
//                        .model(dashScopeModel())
//                        .toolkit(toolkit)
//                        .memory(new InMemoryMemory());
//        return AgentScopeAgent.fromBuilder(builder)
//                .name("github")
//                .description("GitHub specialist for code, issues, and PRs")
//                .instruction("Please respond to the following request:  {input}.")
//                .outputKey("github_key")
//                .build();
//    }
//
//
//    /***
//     *  notion
//     * @return
//     */
//    @Bean
//    public AgentScopeAgent notionAgent(NotionStubTools notionStubTools) {
//        Toolkit toolkit = new Toolkit();
//        toolkit.registerTool(notionStubTools);
//        ReActAgent.Builder builder =
//                ReActAgent.builder()
//                        .name("notion")
//                        .description("Notion specialist for docs and wikis")
//                        .sysPrompt(NOTION_PROMPT)
//                        .model(dashScopeModel())
//                        .toolkit(toolkit)
//                        .memory(new InMemoryMemory());
//        return AgentScopeAgent.fromBuilder(builder)
//                .name("notion")
//                .description("Notion specialist for docs and wikis")
//                .instruction("Please respond to the following request:  {input}.")
//                .outputKey("notion_key")
//                .build();
//    }
//
//    /**
//     * 030622200944
//     * slack
//     *
//     * @param slackStubTools
//     * @return
//     */
//    @Bean
//    public AgentScopeAgent slackAgent(SlackStubTools slackStubTools) {
//        Toolkit toolkit = new Toolkit();
//        toolkit.registerTool(slackStubTools);
//        ReActAgent.Builder builder =
//                ReActAgent.builder()
//                        .name("slack")
//                        .description("Slack specialist for messages and threads")
//                        .sysPrompt(SLACK_PROMPT)
//                        .model(dashScopeModel())
//                        .toolkit(toolkit)
//                        .memory(new InMemoryMemory());
//        return AgentScopeAgent.fromBuilder(builder)
//                .name("slack")
//                .description("Slack specialist for messages and threads")
//                .instruction("Please respond to the following request:  {input}.")
//                .outputKey("slack_key")
//                .build();
//    }
//
//    /**
//     * 路由
//     *
//     * @param githubAgent
//     * @param notionAgent
//     * @param slackAgent
//     * @return
//     */
//    @Bean
//    public AgentScopeRoutingAgent routerAgent(
//            @Qualifier("githubAgent") AgentScopeAgent githubAgent, @Qualifier("notionAgent") AgentScopeAgent notionAgent, @Qualifier("slackAgent") AgentScopeAgent slackAgent) {
//        return AgentScopeRoutingAgent.builder()
//                .name("router")
//                .model(dashScopeModel())
//                .description(
//                        "Routes queries to GitHub, Notion, and/or Slack specialists based on"
//                                + " relevance.")
//                .subAgents(List.of(githubAgent, notionAgent, slackAgent))
//                .build();
//    }
//
//    @Bean
//    public CompiledGraph routingGraph(
//            @Qualifier("githubAgent") AgentScopeAgent githubAgent,
//            @Qualifier("notionAgent") AgentScopeAgent notionAgent,
//            @Qualifier("slackAgent") AgentScopeAgent slackAgent)
//            throws GraphStateException {
//        KeyStrategyFactory keyFactory =
//                () -> {
//                    Map<String, KeyStrategy> strategies = new HashMap<>();
//                    strategies.put("input", new ReplaceStrategy());
//                    strategies.put("query", new ReplaceStrategy());
//                    strategies.put("messages", new AppendStrategy(false));
//                    strategies.put("preprocess_metadata", new ReplaceStrategy());
//                    strategies.put("merged_result", new ReplaceStrategy());
//                    strategies.put("final_answer", new ReplaceStrategy());
//                    strategies.put("postprocess_metadata", new ReplaceStrategy());
//                    strategies.put("github_key", new ReplaceStrategy());
//                    strategies.put("notion_key", new ReplaceStrategy());
//                    strategies.put("slack_key", new ReplaceStrategy());
//                    return strategies;
//                };
//
//        StateGraph graph =
//                new StateGraph("routing_graph", keyFactory)
//                        .addNode("preprocess", node_async(new PreprocessNode()))
//                        .addNode("github", githubAgent.getAndCompileGraph())
//                        .addNode("notion", notionAgent.getAndCompileGraph())
//                        .addNode("slack", slackAgent.getAndCompileGraph())
//                        .addNode("postprocess", node_async(new PostprocessNode()))
//                        .addEdge(START, "preprocess")
//                        .addEdge("preprocess", "github")
//                        .addEdge("preprocess", "notion")
//                        .addEdge("preprocess", "slack")
//                        .addEdge("github", "postprocess")
//                        .addEdge("notion", "postprocess")
//                        .addEdge("slack", "postprocess")
//                        .addEdge("postprocess", END);
//
//        return graph.compile();
//    }
//
//
//    /**
//     * 路由服务
//     *
//     * @param routingGraph
//     * @return
//     */
//    @Bean
//    public RoutingGraphService routingGraphService(CompiledGraph routingGraph) {
//        return new RoutingGraphService(routingGraph);
//    }
//
//
//}
//
