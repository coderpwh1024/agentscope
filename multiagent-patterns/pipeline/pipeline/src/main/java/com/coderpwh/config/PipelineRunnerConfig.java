package com.coderpwh.config;

import com.alibaba.cloud.ai.graph.agent.flow.agent.LoopAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.ParallelAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.SequentialAgent;
import com.coderpwh.service.PipelineService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author coderpwh
 */
@Configuration
public class PipelineRunnerConfig {


    /**
     * 创建流水线服务
     * @param sequentialSqlAgent
     * @param parallelResearchAgent
     * @param loopSqlRefinementAgent
     * @return
     */
    @Bean
    public PipelineService pipelineService(SequentialAgent sequentialSqlAgent,
                                           ParallelAgent parallelResearchAgent,
                                           LoopAgent loopSqlRefinementAgent) {
        return  new PipelineService(sequentialSqlAgent,parallelResearchAgent,loopSqlRefinementAgent);
    }


}
