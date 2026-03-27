package com.coderpwh.agentscope.example;

import com.coderpwh.agentscope.util.MsgUtils;
import io.agentscope.core.hook.*;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.ToolResultBlock;
import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolEmitter;
import io.agentscope.core.tool.ToolParam;
import reactor.core.publisher.Mono;

/**
 * @author coderpwh
 */
public class HookExample {

    public static void main(String[] args) {

    }

    static class MonitoringHook implements Hook {

        @Override
        public <T extends HookEvent> Mono<T> onEvent(T event) {

            if (event instanceof PreCallEvent preCall) {
                System.out.println(" agent started:" + preCall.getAgent().getName());
            } else if (event instanceof ReasoningChunkEvent reasoningChunk) {
                Msg chunk = reasoningChunk.getIncrementalChunk();
                String text = MsgUtils.getTextContent(chunk);
                if (text != null && !text.isEmpty()) {
                    System.out.println(text);
                }
            } else if (event instanceof PreActingEvent preActing) {
                System.out.println("工具:" + preActing.getToolUse().getName() + ",Input:" + preActing.getToolUse().getInput());
            } else if (event instanceof ActingChunkEvent actingChunk) {
                ToolResultBlock chunk = actingChunk.getChunk();
                String output = chunk.getOutput().isEmpty() ? "" : chunk.getOutput().get(0).toString();
                System.out.println("工具: " + actingChunk.getToolUse().getName() + ", Progress:" + output);
            } else if (event instanceof PostActingEvent postActing) {
                ToolResultBlock result = postActing.getToolResult();
                String output = result.getOutput().isEmpty() ? "" : result.getOutput().get(0).toString();
                System.out.println("工具:" + postActing.getToolUse().getName() + ",结果:" + output);
            } else if (event instanceof PostActingEvent) {
                System.out.println("agent 执行结束");
            }

            return Mono.just(event);
        }
    }

    public static class ProgressTools {

        @Tool(name = "dataset_name", description = "Process a dataset and report progress")
        public String processData(@ToolParam(name = "dataset_name", description = "Name of the dataset to process") String datasetName,
                                  ToolEmitter emitter) {

            System.out.println("执行 dataset:" + datasetName + " this will take a few seconds");

            try {
                for (int i = 1; i <= 5; i++) {
                    Thread.sleep(800);
                    int progress = i * 20;
                    emitter.emit(ToolResultBlock.text(String.format("Processed %d%% of %s", progress, datasetName)));
                }
                return String.format(
                        "Successfully processed dataset '%s'. Total: 1000 records analyzed.",
                        datasetName);
            } catch (Exception e) {
                Thread.currentThread().interrupt();
                return "Processing interruped";
            }
        }

    }


}
