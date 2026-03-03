package com.coderpwh.agentscope.example;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.formatter.dashscope.DashScopeChatFormatter;
import io.agentscope.core.memory.InMemoryMemory;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.tool.Toolkit;

import java.io.IOException;

/**
 * @author coderpwh
 */
public class FanoutPipelineExample {

    private static final String PRODUCT_IDEA = "一款移动应用，利用 AI 分析用户每日拍摄的照片，自动生成带有音乐和字幕的个性化视频日记。用户可以以精美剪辑的视频形式回顾自己的一周、一个月或一年。该应用会随着时间推移学习用户偏好，从而不断优化视频风格和音乐选择";


    public static void main(String[] args) throws IOException {

        ExampleUtils.printWelcome("扇形管道示例", "本示例演示了使用 FanoutPipeline 进行并行处理。\n" +
                "一个产品创意将由 3 位独立专家进行评审：\n" +
                "  1. 技术专家（可行性评估）\n" +
                "  2. 用户体验专家（用户体验评估）\n" +
                "  3. 业务分析师（市场价值分析）\n" +
                "\n" +
                "你将看到并发执行与顺序执行的对比效果！");


        String apiKey = ExampleUtils.getDashScopeApiKey();

        System.out.println("开始执行...");
        ReActAgent techExpert =createTechExpert(apiKey);

    }


    /***
     * 创建技术专家
     * @param apiKey
     * @return
     */
    private static ReActAgent createTechExpert(String apiKey) {
        return  ReActAgent.builder().name("技术专家").sysPrompt("你是一位专注于软件架构和 AI 系统的资深技术专家。\n" +
                "请从技术可行性角度对该产品创意进行评审。\n" +
                "你的评审应涵盖以下内容：\n" +
                "\n" +
                "1. 技术挑战与解决方案\n" +
                "2. 所需技术栈与框架\n" +
                "3. 开发复杂度（1-10 分制）\n" +
                "4. 潜在技术风险\n" +
                "\n" +
                "请保持评审简洁（3-5 句话）。\n" +
                "回复请以 'TECHNICAL REVIEW:' 开头。").model(DashScopeChatModel.builder().apiKey(apiKey).modelName("qwen-plus").stream( true).enableThinking(false).formatter(new DashScopeChatFormatter()).build())
                .memory(new InMemoryMemory()).toolkit(new Toolkit()).build();

    }


}
