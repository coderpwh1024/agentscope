package com.coderpwh.agentscope.example;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.hook.TTSHook;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.model.tts.AudioPlayer;
import io.agentscope.core.model.tts.DashScopeRealtimeTTSModel;


/**
 * @author coderpwh
 */
public class TTSExample {


    public static void main(String[] args) {

        String apiKey = "";
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("设置秘钥");
            return;
        }
        System.out.println("========================================");
        System.out.println("  AgentScope TTS Demo");
        System.out.println("========================================");
        System.out.println();


        realtimeAgentWithTTS(apiKey);

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

}
