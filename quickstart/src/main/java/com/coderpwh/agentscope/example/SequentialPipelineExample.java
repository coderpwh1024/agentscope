package com.coderpwh.agentscope.example;

import com.coderpwh.agentscope.util.MsgUtils;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.formatter.dashscope.DashScopeChatFormatter;
import io.agentscope.core.memory.InMemoryMemory;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.pipeline.SequentialPipeline;
import io.agentscope.core.tool.Toolkit;

import java.io.IOException;
import java.time.Duration;

/**
 * @author coderpwh
 */
public class SequentialPipelineExample {


    private static final String SAMPLE_ARTICLE = "人工智能近年来彻底改变了科技行业。机器学习算法现在为从推荐系统到自动驾驶汽车的一切提供支持。虽然人工智能为创新和效率带来了巨大机遇，但它也引发了有关伦理、隐私和就业替代的重要问题。展望未来，在技术进步与社会福祉之间找到正确的平衡，对于可持续发展至关重要";


    public static void main(String[] args) throws IOException {

        ExampleUtils.printWelcome(
                "顺序流水线示例",
                "本示例演示使用 SequentialPipeline 进行内容处理的工作流。\n"
                        + "一篇英文文章将经过 3 个顺序步骤处理：\n"
                        + "  1. 翻译（英文 → 中文）\n"
                        + "  2. 摘要（生成简洁摘要）\n"
                        + "  3. 情感分析（分析情感倾向）\n\n"
                        + "观察每个智能体的输出如何成为下一个智能体的输入！");

        String apikey = ExampleUtils.getOpenAIApiKey();

        System.out.println("开始执行");

        ReActAgent translator = createTranslator(apikey);
        ReActAgent summarizer = createSummarizer(apikey);
        ReActAgent sentimentAnalyzer = createSentimentAnalyzer(apikey);


        SequentialPipeline pipeline = SequentialPipeline
                .builder()
                .addAgent(translator)
                .addAgent(summarizer)
                .addAgent(sentimentAnalyzer)
                .build();

        System.out.println("通道 创建3个智能体:");
        System.out.println("  [1] Translator → [2] Summarizer → [3] Sentiment Analyzer\n");

        Msg inputMsg = Msg
                .builder()
                .role(MsgRole.USER)
                .content(TextBlock.builder().text(SAMPLE_ARTICLE).build())
                .build();

        printSeparator();
        System.out.println("ORIGINAL ARTICLE (English):");
        printSeparator();
        System.out.println(SAMPLE_ARTICLE);
        System.out.println();
        System.out.println("Executing pipeline...\n");

        long startTime = System.currentTimeMillis();
        Msg result = pipeline.execute(inputMsg).block(Duration.ofMinutes(3));
        long executionTime = System.currentTimeMillis() - startTime;

        printSeparator();
        System.out.println("FINAL RESULT:");
        printSeparator();

        if (result != null) {
            String resultText = MsgUtils.getTextContent(result);
            System.out.println(resultText);
        } else {
            System.out.println("[No result returned]");
        }
        System.out.println();

        printSeparator();
        System.out.println("EXECUTION SUMMARY:");
        printSeparator();

        System.out.println("总执行时间：" + executionTime + "ms");
        System.out.println("流水线阶段：3");
        System.out.println("  步骤 1：翻译完成");
        System.out.println("  步骤 2：摘要生成完成");
        System.out.println("  步骤 3：情感分析完成");
        System.out.println();

        System.out.println(
                "这展示了 SequentialPipeline 如何将多个 Agent 串联在一起，\n"
                        + "每个 Agent 处理上一个 Agent 的输出结果。\n");
    }


    private static ReActAgent createTranslator(String apikey) {
        return ReActAgent
                .builder()
                .name("Translator")
                .sysPrompt("""
                        你是一位专业翻译。将给定的英文文本准确翻译成中文。保留原文的含义和语气。只输出翻译后的文本，不需要任何解释。
                        """)
                .model(
                        DashScopeChatModel
                                .builder()
                                .apiKey(apikey)
                                .modelName("qwen-plus")
                                .stream(true)
                                .enableThinking(false)
                                .formatter(new DashScopeChatFormatter())
                                .build()
                )
                .memory(new InMemoryMemory())
                .toolkit(new Toolkit())
                .build();

    }

    private static ReActAgent createSummarizer(String apiKey) {
        return ReActAgent
                .builder()
                .name("Summarizer")
                .sysPrompt("""
                        你是一位专业的内容摘要生成器。用2-3句话对给定文本生成简洁的摘要。提炼主要观点和核心信息。摘要语言与输入文本保持一致。只输出摘要内容，不需要任何额外说明
                        """)
                .model(DashScopeChatModel
                        .builder()
                        .apiKey(apiKey)
                        .modelName("qwen-plus")
                        .stream(true)
                        .enableThinking(false)
                        .formatter(new DashScopeChatFormatter())
                        .build()
                )
                .memory(new InMemoryMemory())
                .toolkit(new Toolkit())
                .build();
    }

    private static ReActAgent createSentimentAnalyzer(String apikey) {
        return ReActAgent
                .builder()
                .name("SentimentAnalyzer")
                .sysPrompt("""
                        你是一位情感分析专家。分析给定文本的情感倾向。将情感分类为：正面、负面、中性或混合。用1-2句话解释你的分类依据。请按以下格式输出结果：
                        情感：[分类]
                        理由：[解释]
                        摘要：[重复输入文本]
                        """)
                .model(DashScopeChatModel
                        .builder()
                        .apiKey(apikey)
                        .modelName("qwen-plus")
                        .stream(true)
                        .enableThinking(false)
                        .formatter(new DashScopeChatFormatter())
                        .build()
                )
                .memory(new InMemoryMemory())
                .toolkit(new Toolkit())
                .build();

    }


    private static void printSeparator() {
        System.out.println("=".repeat(70));
    }


}
