package com.coderpwh.agentscope.example;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.formatter.dashscope.DashScopeChatFormatter;
import io.agentscope.core.hook.*;
import io.agentscope.core.memory.InMemoryMemory;
import io.agentscope.core.message.*;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.tool.multimodal.DashScopeMultiModalTool;
import reactor.core.publisher.Mono;
import java.awt.*;
import java.io.IOException;
import java.util.List;
import io.agentscope.core.tool.Toolkit;
/**
 * @author coderpwh
 */
public class MultiModalToolExample {

    public static void main(String[] args) throws Exception {

        ExampleUtils.printWelcome(
                "多模态工具调用示例",
                "本示例演示如何为 Agent 配备多模态工具。\n"
                        + "该 Agent 具备图像、音频和视频多模态工具");

        String apikey = ExampleUtils.getDashScopeApiKey();

        Toolkit toolkit = new Toolkit();
        toolkit.registerTool(new DashScopeMultiModalTool(apikey));
        printRegisterTools();

        ReActAgent agent = ReActAgent
                .builder()
                .name("MultiModalToolAgent")
                .sysPrompt("你是一个可以使用多模态工具的智能助手,需要时请使用工具来准确回答问题,在使用工具时，请始终说明你正在执行的操作")
                .model(DashScopeChatModel
                        .builder()
                        .apiKey(apikey)
                        .modelName("qwen-plus")
                        .stream(true)
                        .enableThinking(false)
                        .formatter(new DashScopeChatFormatter())
                        .build()

                )
                .hook(new ToolCallLoggingHook())
                .toolkit(toolkit)
                .memory(new InMemoryMemory())
                .build();

        printExamplePrompts();

        ExampleUtils.startChat(agent);
    }

    private static void printRegisterTools() {
        String registeredTools =
                """
                        已注册的工具：
                        - dashscope_text_to_image：根据给定文本生成图片。
                        - dashscope_image_to_text：根据给定图片生成文本。
                        - dashscope_text_to_audio：将给定文本转换为音频。
                        - dashscope_audio_to_text：将给定音频转换为文本。
                        - dashscope_text_to_video：根据给定文本提示生成视频。
                        - dashscope_image_to_video：根据单张输入图片及可选文本提示生成视频。
                        - dashscope_first_and_last_frame_image_to_video：根据首帧图片、尾帧图片及可选文本提示生成过渡视频。
                        - dashscope_video_to_text：分析视频并生成文字描述，或根据视频内容回答问题。
                        """;
        System.out.println(registeredTools);
        System.out.println("\n");
    }

    private static void printExamplePrompts() {
        String examplePrompts =
                """
                        示例提示词：
                        [dashscope_text_to_image]：
                        生成一张黑色狗狗的图片链接。
                        [dashscope_image_to_text]：
                        描述图片链接 'https://dashscope.oss-cn-beijing.aliyuncs.com/images/tiger.png' 的内容。
                        [dashscope_text_to_audio]：
                        将文本 'hello, qwen!' 转换为音频链接。
                        [dashscope_audio_to_text]：
                        将音频链接 'https://dashscope.oss-cn-beijing.aliyuncs.com/samples/audio/paraformer/hello_world_male2.wav' 转换为文本。
                        [dashscope_text_to_video]：
                        生成一段聪明的猫咪在月光下奔跑的视频。
                        [dashscope_image_to_video]：
                        根据图片链接 'https://dashscope.oss-cn-beijing.aliyuncs.com/images/tiger.png' 生成一段老虎在月光下奔跑的视频。
                        [dashscope_first_and_last_frame_image_to_video]：
                        根据首帧图片链接 'https://wanx.alicdn.com/material/20250318/first_frame.png' 和尾帧图片链接 'https://wanx.alicdn.com/material/20250318/last_frame.png'，生成一段黑色小猫好奇地仰望天空的视频。
                        [dashscope_video_to_text]：
                        描述视频链接 'https://help-static-aliyun-doc.aliyuncs.com/file-manage-files/zh-CN/20241115/cqqkru/1.mp4' 的内容。
                        """;
        System.out.println(examplePrompts);
        System.out.println("\n");
    }

    static class ToolCallLoggingHook implements Hook {
        @Override
        public <T extends HookEvent> Mono<T> onEvent(T event) {
            if (event instanceof PreActingEvent preActing) {
                System.out.println("工具:" + preActing.getToolUse().getName() + ",Input:" + preActing.getToolUse().getInput());
            } else if (event instanceof PostActingEvent postActingEvent) {
                ToolResultBlock toolResult = postActingEvent.getToolResult();
                List<ContentBlock> contentBlocks = toolResult.getOutput();
                if (contentBlocks != null && !contentBlocks.isEmpty()) {
                    for (ContentBlock cb : contentBlocks) {
                        if (cb instanceof ImageBlock ib) {
                            Source source = ib.getSource();
                            if (source instanceof URLSource urlSource) {
                                System.out.println("工具结果:" + urlSource.getUrl());
                            } else if (source instanceof Base64Source base64Source) {
                                System.out.println("工具结果:" + base64Source.getData());
                            }
                        } else if (cb instanceof AudioBlock ab) {
                            Source source = ab.getSource();
                            if (source instanceof URLSource urlSource) {
                                System.out.println("工具结果:" + urlSource.getUrl());
                            } else if (source instanceof Base64Source base64Source) {
                                System.out.println("工具结果:\n Audio Base64 data:" + base64Source.getData());
                            }
                        } else if (cb instanceof VideoBlock vb) {
                            Source source = vb.getSource();
                            if (source instanceof URLSource urlSource) {
                                System.out.println("Tool Result:\n +Video URL:" + urlSource.getUrl());
                            } else if (source instanceof Base64Source base64Source) {
                                System.out.println("Tool Result:\n Video Base64 data:" + base64Source.getData());
                            }

                        } else if (cb instanceof TextBlock tb) {
                            System.out.println("工具结果\n Text:" + tb.getText());
                        }

                    }

                }
            }
            return Mono.just(event);
        }
    }


}
