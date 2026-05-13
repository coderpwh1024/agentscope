package com.coderpwh.routing.graph;

import com.alibaba.cloud.ai.agent.agentscope.AgentScopeAgent;
import com.alibaba.cloud.ai.agent.agentscope.flow.AgentScopeRoutingAgent;
import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.KeyStrategy;
import com.alibaba.cloud.ai.graph.KeyStrategyFactory;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.state.strategy.AppendStrategy;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import com.coderpwh.routing.graph.node.PostprocessNode;
import com.coderpwh.routing.graph.node.PreprocessNode;
import com.coderpwh.routing.graph.tools.GitHubStubTools;
import com.coderpwh.routing.graph.tools.NotionStubTools;
import com.coderpwh.routing.graph.tools.SlackStubTools;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.memory.InMemoryMemory;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.model.Model;
import io.agentscope.core.tool.Toolkit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.alibaba.cloud.ai.graph.StateGraph.END;
import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;

@Configuration
public class RoutingGraphConfig {

    private static final String GITHUB_PROMPT =
            """
                    你是一位 GitHub 专家。请通过搜索代码仓库、Issue 和 Pull Request，\
                    回答关于代码、API 参考和实现细节的问题。
                    请回应以下请求：{github_input}
                    """;

    private static final String NOTION_PROMPT =
            """
                    你是一位 Notion 专家。请通过搜索组织的 Notion 工作区，回答关于内部流程、规章制度和团队\
                    文档的问题。
                    请回应以下请求：{notion_input}
                    """;

    private static final String SLACK_PROMPT =
            """
                    你是一位 Slack 专家。请通过搜索团队成员分享知识和解决方案的相关话题串和讨论，\
                    回答相关问题。
                    请回应以下请求：{slack_input}
                    """;
    private final GitHubStubTools gitHubStubTools;
    private final NotionStubTools notionStubTools;

    public RoutingGraphConfig(GitHubStubTools gitHubStubTools, NotionStubTools notionStubTools) {
        this.gitHubStubTools = gitHubStubTools;
        this.notionStubTools = notionStubTools;
    }


    private static Model dashScopeModel() {
        String key = System.getenv("AI_DASHSCOPE_API_KEY");
        return DashScopeChatModel.builder().apiKey(key).modelName("qwen-plus").build();
    }


    /***
     * github
     * @return
     */
    @Bean
    public AgentScopeAgent githubAgent() {
        Toolkit toolkit = new Toolkit();
        toolkit.registerTool(gitHubStubTools);

        ReActAgent.Builder builder = ReActAgent.builder()
                .name("github")
                .description("Github specialist for code,issues,and PRS")
                .sysPrompt(GITHUB_PROMPT)
                .model(dashScopeModel())
                .toolkit(toolkit)
                .memory(new InMemoryMemory());

        return AgentScopeAgent.fromBuilder(builder)
                .name("github")
                .description("Github specialist for code,issues,and PRS")
                .instruction("please repoond to the following request:{github_input}.")
                .outputKey("github_key")
                .build();
    }


    /***
     *  notion
     * @return
     */
    @Bean
    public AgentScopeAgent notionAgent() {
        Toolkit toolkit = new Toolkit();
        toolkit.registerTool(notionStubTools);

        ReActAgent.Builder builder = ReActAgent.builder()
                .name("notion")
                .description("notion specialist for docs and wikis")
                .sysPrompt(NOTION_PROMPT)
                .model(dashScopeModel())
                .toolkit(toolkit)
                .memory(new InMemoryMemory());

        return AgentScopeAgent.fromBuilder(builder)
                .name("notion")
                .description("Notion specialist for docs and wikis")
                .instruction("Please respond to the following request: {notion_input}")
                .outputKey("notion_key")
                .build();
    }


    /**
     * slack
     *
     * @param slackStubTools
     * @return
     */
    @Bean
    public AgentScopeAgent slackAgent(SlackStubTools slackStubTools) {
        Toolkit toolkit = new Toolkit();
        toolkit.registerTool(slackStubTools);

        ReActAgent.Builder builder = ReActAgent.builder()
                .name("slack")
                .description("Slack specialist for messages and threads")
                .sysPrompt(SLACK_PROMPT)
                .model(dashScopeModel())
                .toolkit(toolkit)
                .memory(new InMemoryMemory());

        return AgentScopeAgent.fromBuilder(builder)
                .name("slack")
                .description("Slack specialist for messages and threads")
                .instruction("Please respond to the following request: {slack_input}.")
                .outputKey("slack_key")
                .build();
    }


    /**
     * 路由
     *
     * @param githubAgent
     * @param notionAgent
     * @param slackAgent
     * @return
     */
    @Bean
    public AgentScopeRoutingAgent routingAgent(AgentScopeAgent githubAgent, AgentScopeAgent notionAgent, AgentScopeAgent slackAgent) {
        return AgentScopeRoutingAgent.builder()
                .name("router")
                .model(dashScopeModel())
                .description("Routes queries to GitHub, Notion, and/or Slack specialists based on"
                        + " relevance.")
                .subAgents(List.of(githubAgent, notionAgent, slackAgent))
                .build();
    }


    /***
     * 路由图
     * @param routingAgent
     * @return
     * @throws GraphStateException
     */
    @Bean
    public CompiledGraph routingGraph(AgentScopeRoutingAgent routingAgent) throws GraphStateException {

        KeyStrategyFactory keyFactory = () -> {
            Map<String, KeyStrategy> strategies = new HashMap<>();
            strategies.put("input", new ReplaceStrategy());
            strategies.put("query", new ReplaceStrategy());
            strategies.put("messages", new AppendStrategy(false));
            strategies.put("preprocess_metadata", new ReplaceStrategy());
            strategies.put("merged_result", new ReplaceStrategy());
            strategies.put("final_answer", new ReplaceStrategy());
            strategies.put("postprocess_metadata", new ReplaceStrategy());
            strategies.put("github_key", new ReplaceStrategy());
            strategies.put("notion_key", new ReplaceStrategy());
            strategies.put("slack_key", new ReplaceStrategy());
            return strategies;

        };

        StateGraph graph = new StateGraph("routing_graph", keyFactory)
                .addNode("preprocess", node_async(new PreprocessNode()))
                .addNode("routing", routingAgent.getAndCompileGraph())
                .addNode("postprocess", node_async(new PostprocessNode()))
                .addEdge("START", "preprocess")
                .addEdge("preprocess", "routing")
                .addEdge("routing", "postprocess")
                .addEdge("postproces", END);

        return graph.compile();
    }




}

