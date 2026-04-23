package com.coderpwh.agentscope.example;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.hook.TTSHook;
import io.agentscope.core.message.*;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.model.tts.*;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.core.tool.multimodal.DashScopeMultiModalTool;


/**
 * @author coderpwh
 */
public class TTSExample {


    public static void main(String[] args) {

        String apiKey = " ";
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("设置秘钥");
            return;
        }
        System.out.println("========================================");
        System.out.println("  AgentScope TTS Demo");
        System.out.println("========================================");
        System.out.println();


        realtimeAgentWithTTS(apiKey);

//        standaloneTTSModel(apiKey);

//        standaloneRealtimeTTSDemo(apiKey);

//        agentWithTTSTool(apiKey);

    }

    private static void realtimeAgentWithTTS(String apiKey) {
        System.out.println("开启TTS");
        System.out.println();


        DashScopeRealtimeTTSModel ttsModel = DashScopeRealtimeTTSModel
                .builder()
                .apiKey(apiKey)
                .modelName("qwen3-tts-flash-realtime")
                .voice("Cherry")
                .sampleRate(24000)
                .format("pcm")
                .build();


        AudioPlayer player = AudioPlayer
                .builder()
                .sampleRate(24000)
                .sampleSizeInBits(16)
                .channels(1)
                .signed(true)
                .bigEndian(false)
                .build();

        TTSHook ttsHook = TTSHook.builder()
                .ttsModel(ttsModel)
                .audioPlayer(player)
                .build();

        DashScopeChatModel chatModel = DashScopeChatModel
                .builder()
                .apiKey(apiKey)
                .modelName("qwen-plus")
                .build();

        ReActAgent agent = ReActAgent
                .builder()
                .name("Assistant")
                .sysPrompt("你是一个友善的助手，请用不多于30字的简洁文字回复")
                .model(chatModel)
                .hook(ttsHook)
                .maxIters(3)
                .build();

        System.out.println("User: 告诉我一个有趣的冷知识");
        System.out.println("Assistant (开始实时): ");

        Msg userMsg = Msg.builder()
                .role(MsgRole.USER)
                .content(TextBlock.builder().text("告诉我一个有趣的冷知识").build())
                .build();

        Msg response = agent.call(userMsg).block();

        if (response != null) {
            System.out.println(response.getTextContent());
        }

        // 7. Clean up
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        ttsHook.stop();

        System.out.println();
    }


    private static void standaloneTTSModel(String apiKey) {
        System.out.println("开启TTS");
        System.out.println();

        System.out.println("非流式");

        DashScopeTTSModel ttsModel = DashScopeTTSModel
                .builder()
                .apiKey(apiKey)
                .modelName("qwen3-tts-flash")
                .voice("Cherry")
                .build();

        TTSOptions options = TTSOptions.builder().sampleRate(24000).format("wav").language("Auto").build();

        String text = "你好, 欢迎来到 AgentScope Java 的 TTS demo.";
        System.out.println("Text: " + text);

        TTSResponse response = ttsModel.synthesize(text, options).block();

        if (response != null) {
            System.out.println("请求ID:" + response.getRequestId());
            if (response.getAudioData() != null) {
                System.out.println("声音数据:" + response.getAudioData().length + "  bytes, playing...");

                AudioPlayer nonStreamPlayer = AudioPlayer
                        .builder()
                        .sampleRate(24000)
                        .sampleSizeInBits(16)
                        .channels(1)
                        .signed(true)
                        .bigEndian(false)
                        .build();


                nonStreamPlayer.start();
                nonStreamPlayer.play(response.toAudioBlock());
                nonStreamPlayer.drain();

                try {

                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                nonStreamPlayer.stop();
                System.out.println("播放完成");
            }
        } else if (response.getAudioUrl() != null) {
            System.out.println("音频URL:" + response.getAudioUrl());
        }
    }

    private static void standaloneRealtimeTTSDemo(String apiKey) {
        System.out.println("开启TTS 推拉");
        System.out.println();

        DashScopeRealtimeTTSModel ttsModel = DashScopeRealtimeTTSModel
                .builder()
                .apiKey(apiKey)
                .modelName("qwen3-tts-flash-realtime")
                .voice("Cherry")
                .sampleRate(24000)
                .format("pcm")
                .mode(DashScopeRealtimeTTSModel.SessionMode.SERVER_COMMIT)
                .languageType("Auto")
                .build();

        AudioPlayer player = AudioPlayer
                .builder()
                .sampleRate(24000)
                .sampleSizeInBits(16)
                .channels(1)
                .signed(true)
                .bigEndian(false)
                .build();

        player.stop();

        System.out.println("开启TTS  会话");
        ttsModel.startSession();

        System.out.println("音频流式");

        ttsModel.getAudioStream().doOnNext(audio -> {
                    player.play(audio);
                }).doOnComplete(() -> System.out.println("播放完成"))
                .subscribe();

        String[] textChunks = {"你好，", "我是", "你的", "语音", "助手，", "很高兴", "为你", "服务！"};
        System.out.println("文本流式");
        for (String chunk : textChunks) {
            System.out.println(chunk);
            ttsModel.push(chunk);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        System.out.println();
        System.out.println("开始结束会话");

        ttsModel.finish().blockLast();

        player.drain();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        player.stop();
        ttsModel.close();

        System.out.println("结束");
        System.out.println();


    }

    private static void agentWithTTSTool(String apiKey) {
        System.out.println("开启TTS 工具");
        System.out.println();

        DashScopeChatModel chatModel = DashScopeChatModel
                .builder()
                .apiKey(apiKey)
                .modelName("qwen-plus")
                .stream(true)
                .build();


        DashScopeMultiModalTool multiModalTool = new DashScopeMultiModalTool(apiKey);
        Toolkit toolkit = new Toolkit();
        toolkit.registerTool(multiModalTool);

        ReActAgent agent = ReActAgent.builder()
                .name("MultiModalAssistant")
                .sysPrompt("你是一个多模态助手,当用户要求你朗读或生成音频时,请使用 dashscope_text_to_audio 工具")
                .model(chatModel)
                .toolkit(toolkit)
                .maxIters(3)
                .build();

        System.out.println("开启音频");

        Msg userMsg =
                Msg.builder()
                        .role(MsgRole.USER)
                        .content(
                                TextBlock.builder()
                                        .text("Please say 'Welcome to AgentScope' in audio")
                                        .build())
                        .build();

        Msg response = agent.call(userMsg).block();

        if (response != null) {
            System.out.println("agent 返回:");
            boolean foundAudio = false;

            for (ContentBlock block : response.getContent()) {
                if (block instanceof TextBlock textBlock) {
                    System.out.println(" Text:" + textBlock.getText());
                } else if (block instanceof AudioBlock audio) {
                    foundAudio = true;
                    System.out.println("音频开始生成");
                    System.out.println("类型:" + audio.getSource().getClass().getSimpleName());

                }
            }
            if (!foundAudio) {
                System.out.println("智能体开始返回音频");
                System.out.println("返回结果:" + response.getTextContent());
            }

        }

        System.out.println();


    }


}
