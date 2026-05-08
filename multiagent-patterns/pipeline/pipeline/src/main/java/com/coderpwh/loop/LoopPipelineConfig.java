package com.coderpwh.loop;

import com.alibaba.cloud.ai.agent.agentscope.AgentScopeAgent;
import com.alibaba.cloud.ai.graph.agent.Agent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.LoopAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.SequentialAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.loop.LoopMode;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.memory.InMemoryMemory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.agentscope.core.model.Model;

import java.util.List;

/**
 * @author coderpwh
 */
@Configuration
public class LoopPipelineConfig {

    private static final Logger log = LoggerFactory.getLogger(LoopPipelineConfig.class);

    private static final double QUALITY_THRESHOLD = 0.5;

    private static final String SQL_GENERATOR_PROMPT = """
            你是一名 MySQL 数据库专家。根据用户的自然语言请求,输出对应的 SQL 语句。
            仅输出有效的 MySQL SQL。不要包含任何解释。
            """;


    private static final String SQL_RATER_PROMPT =
            """
                    你是一名 SQL 质量审查员。给定用户的自然语言请求和生成的 SQL,
                    输出一个介于 0 到 1 之间的浮点数分数。该分数表示 SQL 与用户意图的匹配程度。
                    仅输出该数字,不要包含任何其他文本。示例:0.85
                            """;


    @Bean("loopSqlRefinementAgent")
    public LoopAgent loopSqlRefinementAgent(Model dashScopeChatModel) {

        ReActAgent.Builder sqlGenBuilder =
                ReActAgent.builder()
                        .name("sql_generator")
                        .model(dashScopeChatModel)
                        .description("Converts natural language to MySQL SQL")
                        .sysPrompt(SQL_GENERATOR_PROMPT)
                        .memory(new InMemoryMemory());

        AgentScopeAgent sqlGenAgent = AgentScopeAgent.fromBuilder(sqlGenBuilder)
                .name("sql_generator")
                .description("Converts natural language to MySQL SQL")
                .instruction("{input}")
                .includeContents(false)
                .outputKey("sql")
                .build();


        ReActAgent.Builder sqlRaterBuilder =
                ReActAgent.builder()
                        .name("sql_rater")
                        .model(dashScopeChatModel)
                        .description("cores SQL against user intent")
                        .sysPrompt(SQL_RATER_PROMPT)
                        .memory(new InMemoryMemory());

        AgentScopeAgent sqlRatingAgent =
                AgentScopeAgent.fromBuilder(sqlRaterBuilder)
                        .name("sql_rater")
                        .description("Scores SQL against user intent")
                        .instruction(
                                "Here's the generated SQL:\n"
                                        + " {sql}.\n\n"
                                        + " Here's the original user request:\n"
                                        + " {input}.")
                        .includeContents(false)
                        .outputKey("score")
                        .build();


        /**
         * 创建一个循环代理
         */
        SequentialAgent sqlAgent = SequentialAgent.builder()
                .name("sql_agent")
                .description("Generates SQL and scores its quality")
                .subAgents(List.of(sqlGenAgent, sqlRatingAgent))
                .build();


        return LoopAgent.builder()
                .name("loop_sql_refinement_agent")
                .description(
                        "Iteratively refines SQL until quality score exceeds " + QUALITY_THRESHOLD)
                .subAgent(sqlAgent)
                .loopStrategy(
                        LoopMode.condition(messages -> {
                            if (messages == null || messages.isEmpty()) {
                                return false;
                            }
                            String text = messages.get(messages.size() - 1).getText();
                            if (text == null || text.isBlank()) {
                                return false;
                            }

                            try {
                                double score = Double.parseDouble(text.trim());
                                boolean satisfied = score > QUALITY_THRESHOLD;
                                if (satisfied) {
                                    log.debug(
                                            "SQL quality score {} exceeds threshold {},"
                                                    + " stopping loop",
                                            score,
                                            QUALITY_THRESHOLD);
                                }
                                return satisfied;
                            } catch (Exception e) {
                                log.debug("Could not parse score from: {}", text);
                                return false;

                            }
                        })
                )
                .build();
    }


}
