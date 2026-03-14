package com.coderpwh.agentscope.example;

import com.coderpwh.agentscope.util.MsgUtils;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.formatter.dashscope.DashScopeChatFormatter;
import io.agentscope.core.memory.InMemoryMemory;
import io.agentscope.core.message.*;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.model.GenerateOptions;
import io.agentscope.core.tool.Toolkit;

import java.io.IOException;
import java.util.List;

/**
 * @author coderpwh
 */
public class VisionExample {

    public static void main(String[] args) throws IOException {

        ExampleUtils.printWelcome(
                "Vision Example",
                "This example demonstrates how to use vision capabilities.\n"
                        + "The agent can analyze images and describe what it sees.\n"
                        + "\nNote: DashScope vision requires Base64-encoded images for best"
                        + " compatibility.");

        String apiKey = ExampleUtils.getDashScopeApiKey();


        ReActAgent agent = ReActAgent
                .builder()
                .name("VisionAssistant")
                .sysPrompt("你是一个具备视觉能力的智能助手。请仔细分析" + " images carefully and provide accurate descriptions.\"")
                .model(DashScopeChatModel
                        .builder()
                        .apiKey(apiKey)
                        .modelName("qwen-vl-max")
                        .stream(true)
                        .formatter(new DashScopeChatFormatter())
                        .defaultOptions(GenerateOptions.builder().build()).build())
                .memory(new InMemoryMemory())
                .toolkit(new Toolkit())
                .build();


        demonstrateVision(agent);

        System.out.println("\n" + "=".repeat(80));
        System.out.println("Interactive Mode");
        System.out.println("=".repeat(80));
        System.out.println("You can now chat with the agent normally.");
        System.out.println("To analyze more images, describe them or ask questions!");
        System.out.println("=".repeat(80) + "\n");

        // Start interactive chat
        ExampleUtils.startChat(agent);
    }

    private static void demonstrateVision(ReActAgent agent) {

        System.out.println("\n" + "=".repeat(80));
        System.out.println("视觉能力演示");
        System.out.println("=".repeat(80));
        System.out.println("使用一张简单图片进行测试（20x20 红色方块 PNG）");
        System.out.println("问题：这张图片是什么颜色？\n");

        try {
            String redSquareBase64 =
                    "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAIAAAAC64paAAAAFklEQVR42mP8z8DAwMj4n4FhFIw"
                            + "CMgBmBQEAAhUCYwAAAABJRU5ErkJggg==";

            Msg userMsg =
                    Msg.builder()
                            .role(MsgRole.USER)
                            .content(
                                    List.of(
                                            TextBlock.builder()
                                                    .text(
                                                            "What color is this image? Please"
                                                                    + " describe it.")
                                                    .build(),
                                            ImageBlock.builder()
                                                    .source(
                                                            Base64Source.builder()
                                                                    .data(redSquareBase64)
                                                                    .mediaType("image/png")
                                                                    .build())
                                                    .build()))
                            .build();


            System.out.println("Sending request to vision model...");

            Msg response = agent.call(userMsg).block();

            System.out.println("\nAgent Response:");
            System.out.println("-".repeat(80));
            System.out.println(MsgUtils.getTextContent(response));
            System.out.println("-".repeat(80));
            System.out.println("\nVision capability verified successfully!");
        } catch (Exception e) {
            System.err.println("\nError analyzing image: " + e.getMessage());
            System.err.println("\nThis may indicate an issue with:");
            System.err.println("  1. API key or model access");
            System.err.println("  2. Network connectivity");
            System.err.println("  3. Model configuration");
            System.err.println(
                    "\nDon't worry - you can still test with text-only questions below.\n");
        }

    }

}
