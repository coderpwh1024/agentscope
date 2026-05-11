package com.coderpwh.service;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.agent.flow.agent.LoopAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.ParallelAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.SequentialAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import org.springframework.ai.chat.messages.Message;

import java.util.Optional;

/**
 * @author coderpwh
 */
public class PipelineService {

    private static final String INPUT_KEY = "input";
    private static final String SQL_KEY = "sql";
    private static final String SCORE_KEY = "score";
    private static final String RESEARCH_REPORT_KEY = "research_report";

    private final SequentialAgent sequentialSqlAgent;
    private final ParallelAgent parallelResearchAgent;
    private final LoopAgent loopSqlRefinementAgent;

    public PipelineService(
            SequentialAgent sequentialSqlAgent,
            ParallelAgent parallelResearchAgent,
            LoopAgent loopSqlRefinementAgent) {
        this.sequentialSqlAgent = sequentialSqlAgent;
        this.parallelResearchAgent = parallelResearchAgent;
        this.loopSqlRefinementAgent = loopSqlRefinementAgent;
    }


    /**
     * 运行顺序流水线
     * @param userInput
     * @return
     * @throws GraphRunnerException
     */
    public SequentialResult runSequential(String userInput) throws GraphRunnerException {
        Optional<OverAllState> resulOpt = sequentialSqlAgent.invoke(userInput);

        if(resulOpt.isEmpty()){
            return  new SequentialResult(userInput,null,null);
        }

        OverAllState state = resulOpt.get();
        String sql = extractText(state.value(SQL_KEY));
        String score = extractText(state.value(SCORE_KEY));
        return new SequentialResult(userInput, sql, score);
    }


    private static  String extractText(Optional<Object> valueOpt){
        if(valueOpt.isEmpty()){
            return null;
        }
        Object v = valueOpt.get();

        if (v instanceof Message message) {
            return message.getText();
        }
        return v != null ? v.toString() : null;
    }



    public record SequentialResult(String input, String sql, String score) {}

    public record ParallelResult(String input, String researchReport) {}

    public record LoopResult(String input, String sql, String score) {}







}
