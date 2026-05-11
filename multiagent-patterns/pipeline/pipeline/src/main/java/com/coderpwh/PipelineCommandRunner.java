package com.coderpwh;

import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.coderpwh.service.PipelineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * @author coderpwh
 */

@Component
@ConditionalOnProperty(name = "pipeline.runner.enabled", havingValue = "true")
public class PipelineCommandRunner implements ApplicationRunner {


    private static final Logger log = LoggerFactory.getLogger(PipelineCommandRunner.class);


    private final PipelineService pipelineService;


    public PipelineCommandRunner(PipelineService pipelineService) {
        this.pipelineService = pipelineService;
    }


    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("=== Pipeline Command Runner (test) ===");

    }


    /***
     * 运行顺序流水线
     */
    private void runSequentialDemo() {
        String input = "List all orders from the last 30 days with total amount greater than 500";
        log.info("--- SequentialAgent demo ---");
        log.info("Input: {}", input);

        try {
            PipelineService.SequentialResult result = pipelineService.runSequential(input);
            log.info("SQL: {}", result.sql());
            log.info("Score: {}", result.score());
        } catch (Exception e) {
            log.error("Error: {}", e.getMessage());
        }
    }


    /***
     * 运行并行流水线
     */
    private void runParallelDemo() {
        String input = "AI agents in enterprise software";
        log.info("--- ParallelAgent demo ---");
        log.info("Input: {}", input);

        try {
            PipelineService.ParallelResult result = pipelineService.runParallel(input);
            log.info("Research report (excerpt): {}", truncate(result.researchReport(), 400));
        } catch (Exception e) {
            log.error("Parallel pipeline failed", e);
        }
    }

    private void runLoopDemo() {
        String input = "Find customers who placed more than 3 orders in 2024";
        log.info("--- LoopAgent demo ---");
        log.info("Input: {}", input);
        try {
            PipelineService.LoopResult result = pipelineService.runLoop(input);
            log.info("SQL: {}", result.sql());
            log.info("Score: {}", result.score());
        } catch (GraphRunnerException e) {
            log.error("Loop pipeline failed", e);
        }
    }




    /***
     *
     * @param s
     * @param maxLen
     * @return
     */
    private static String truncate(String s, int maxLen) {
        if (s == null) {
            return "null";
        }
        if (s.length() <= maxLen) {
            return s;
        }
        return s.substring(0, maxLen) + "....";
    }


}
