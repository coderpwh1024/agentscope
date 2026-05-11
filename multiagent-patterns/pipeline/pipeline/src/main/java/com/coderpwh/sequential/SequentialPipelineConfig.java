package com.coderpwh.sequential;

import com.alibaba.cloud.ai.agent.agentscope.AgentScopeAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.SequentialAgent;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.memory.InMemoryMemory;
import io.agentscope.core.model.Model;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author coderpwh
 */
@Configuration
public class SequentialPipelineConfig {


    private static final String SQL_GENERATOR_PROMPT = """
            你是一名 MySQL 数据库专家。根据用户的自然语言请求，输出相应的 SQL 语句。
            只输出有效的 MySQL SQL。不要包含任何解释。
            """;


    private static final String SQL_RATER_PROMPT =
            """
                     你是一名 SQL 质量审核员。根据用户的自然语言请求和生成的 SQL，
                     输出一个介于 0 到 1 之间的浮点数评分。该评分表示 SQL 与用户意图的匹配程度。
                     仅输出该数字，不要包含任何其他文本。示例：0.85
                    """;


    @Bean("sequentialSqlAgent")
    public SequentialAgent sequentialSqlAgent(Model dashScopeChatModel) {


        // SQL 生成器
        ReActAgent.Builder sqlGenBuilder = ReActAgent.builder()
                .name("sql_generator")
                .description("Converts natural language to MySQL SQL")
                .model(dashScopeChatModel)
                .sysPrompt(SQL_GENERATOR_PROMPT)
                .memory(new InMemoryMemory());


        // SQL 审核
        AgentScopeAgent sqlGenAgent = AgentScopeAgent.
                fromBuilder(sqlGenBuilder)
                .name("sql_generator")
                .description("Converts natural language to MySQL SQL")
                .instruction("{input}")
                .includeContents(false)
                .outputKey("sql")
                .build();


        // SQL 评分
        ReActAgent.Builder sqlRaterBuilder = ReActAgent.builder()
                .name("sql_rater")
                .model(dashScopeChatModel)
                .description("Scores SQL against user intent")
                .sysPrompt(SQL_RATER_PROMPT)
                .memory(new InMemoryMemory());

        AgentScopeAgent sqlRatingAgent = AgentScopeAgent.fromBuilder(sqlRaterBuilder)
                .name("sql_rater")
                .description("Scores SQL against user intent")
                .instruction("Here's the generated SQL:\n"
                        + " {sql}.\n\n"
                        + " Here's the original user request:\n"
                        + " {input}.")
                .includeContents(false)
                .outputKey("score")
                .build();

        return SequentialAgent.builder()
                .name("sequential_sql_agent")
                .description("Natural language to SQL pipeline: generates SQL and scores its quality")
                .subAgents(List.of(sqlGenAgent, sqlRatingAgent))
                .build();


    }


}
