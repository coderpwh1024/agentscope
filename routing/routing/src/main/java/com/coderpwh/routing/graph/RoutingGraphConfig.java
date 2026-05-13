package com.coderpwh.routing.graph;

import com.alibaba.cloud.ai.agent.agentscope.AgentScopeAgent;
import com.coderpwh.routing.graph.tools.GitHubStubTools;
import com.coderpwh.routing.graph.tools.NotionStubTools;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.memory.InMemoryMemory;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.model.Model;
import io.agentscope.core.tool.Toolkit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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


}

