package com.coderpwh.parallel;

import com.alibaba.cloud.ai.agent.agentscope.AgentScopeAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.ParallelAgent;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.memory.InMemoryMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.agentscope.core.model.Model;

import java.util.List;

/**
 * @author coderpwh
 */
@Configuration
public class ParallelPipelineConfig {

    private static final String TECH_RESEARCH_PROMPT =
            """
                    你是一名技术分析师。请从技术角度对给定主题进行研究。
                    提供一份简明的 2-3 段分析,涵盖:关键技术、发展趋势和创新点。
                    请仅聚焦于技术层面。
                    """;

    private static final String FINANCE_RESEARCH_PROMPT =
            """
                    你是一名金融分析师。请从金融和商业角度对给定主题进行研究。
                    提供一份简明的 2-3 段分析,涵盖:市场规模、投资趋势、商业模式。
                    请仅聚焦于金融和商业层面。
                    """;

    private static final String MARKET_RESEARCH_PROMPT =
            """
                    你是一名市场分析师。请从行业和市场角度对给定主题进行研究。
                    提供一份简明的 2-3 段分析,涵盖:竞争格局、增长驱动因素、面临的挑战。
                    请仅聚焦于市场层面。
                    """;


    @Bean("parallelResearchAgent")
    public ParallelAgent parallelResearchAgent(Model dashScopeChatModel) {

        ReActAgent.Builder techBuilder = ReActAgent.builder()
                .name("tech_researcher")
                .model(dashScopeChatModel)
                .description("Researches from technology perspective")
                .sysPrompt(TECH_RESEARCH_PROMPT)
                .memory(new InMemoryMemory());

        AgentScopeAgent techResearcher = AgentScopeAgent.fromBuilder(techBuilder)
                .name("tech_researcher")
                .description("Researches from technology perspective")
                .instruction("Research the following topic: {input}.")
                .includeContents(false)
                .outputKey("tech_analysis")
                .build();

        ReActAgent.Builder financeBuilder = ReActAgent.builder()
                .name("finance_researcher")
                .model(dashScopeChatModel)
                .description("Researches from finance perspective")
                .sysPrompt(FINANCE_RESEARCH_PROMPT)
                .memory(new InMemoryMemory());

        AgentScopeAgent financeResearcher = AgentScopeAgent.fromBuilder(financeBuilder)
                .name("finance_researcher")
                .description("Researches from finance perspective")
                .instruction("Research the following topic: {input}.")
                .includeContents(false)
                .outputKey("finance_analysis")
                .build();

        ReActAgent.Builder marketBuilder = ReActAgent.builder()
                .name("market_researcher")
                .model(dashScopeChatModel)
                .description("Researches from market perspective")
                .sysPrompt(MARKET_RESEARCH_PROMPT)
                .memory(new InMemoryMemory());

        AgentScopeAgent marketResearcher = AgentScopeAgent.fromBuilder(marketBuilder)
                .name("market_researcher")
                .description("Researches from market perspective")
                .instruction("Research the following topic: {input}.")
                .includeContents(false)
                .outputKey("market_analysis")
                .build();

        return ParallelAgent.builder()
                .name("parallel_researcher")
                .description("Researches from multiple perspectives")
                .subAgents(List.of(techResearcher, financeResearcher, marketResearcher))
                .mergeStrategy(new ParallelAgent.DefaultMergeStrategy())
                .mergeOutputKey("analysis")
                .maxConcurrency(3)
                .build();
    }

}
