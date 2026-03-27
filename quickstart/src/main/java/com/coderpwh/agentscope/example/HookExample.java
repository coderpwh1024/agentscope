package com.coderpwh.agentscope.example;

import com.coderpwh.agentscope.util.MsgUtils;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.formatter.dashscope.DashScopeChatFormatter;
import io.agentscope.core.hook.*;
import io.agentscope.core.memory.InMemoryMemory;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.ToolResultBlock;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolEmitter;
import io.agentscope.core.tool.ToolParam;
import io.agentscope.core.tool.Toolkit;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.List;

/**
 * @author coderpwh
 */
public class HookExample {

    public static void main(String[] args) throws IOException {
        ExampleUtils.printWelcome(
                "Hook 示例",
                "本示例演示用于监控 Agent 执行过程的 Hook 系统。\n"
                        + "您将看到所有 Agent 活动的详细日志，包括推理过程"
                        + "和工具调用。");

        String apikey = ExampleUtils.getDashScopeApiKey();

        Hook monitoringHook = new MonitoringHook();

        Toolkit toolkit = new Toolkit();
        toolkit.registerTool(new ProgressTools());

        System.out.println("注册的工具");
        System.out.println("- 执行数据");

        ReActAgent agent = ReActAgent
                .builder()
                .name("HookAgent")
                .sysPrompt("你是一个智能助手。在处理数据时，请使用process_data 工具")
                .model(DashScopeChatModel
                        .builder()
                        .apiKey(apikey)
                        .modelName("qwen-plus")
                        .stream(true)
                        .enableThinking(true)
                        .formatter(new DashScopeChatFormatter())
                        .build()
                )
                .toolkit(toolkit)
                .memory(new InMemoryMemory())
                .hooks(List.of(monitoringHook))
                .build();

        System.out.println("开始执行:");
        ExampleUtils.startChat(agent);
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
