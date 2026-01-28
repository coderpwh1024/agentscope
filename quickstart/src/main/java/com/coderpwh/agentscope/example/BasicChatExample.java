package com.coderpwh.agentscope.example;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.formatter.dashscope.DashScopeChatFormatter;
import io.agentscope.core.memory.InMemoryMemory;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.model.GenerateOptions;
import io.agentscope.core.tool.Toolkit;


import java.io.IOException;

/**
 * @author coderpwh
 */
public class BasicChatExample {


    public static void main(String[] args) throws IOException {


        String apiKey = ExampleUtils.getDashScopeApiKey();

        ReActAgent agent = ReActAgent
                .builder()
                .name("Assistant").
                sysPrompt("You are a helpful AI assistant. Be friendly and concise.")
                .model(
                        DashScopeChatModel.builder()
                        .apiKey(apiKey)
                        .modelName("qwen-plus")
                        .stream(true)
                        .enableThinking(true)
                        .formatter(new DashScopeChatFormatter())
                        .defaultOptions(
                                GenerateOptions.builder()
                                        .thinkingBudget(1024)
                                        .build())
                        .build())
                .memory(new InMemoryMemory())
                .toolkit(new Toolkit())
                .build();

        ExampleUtils.startChat(agent);

    }
}
