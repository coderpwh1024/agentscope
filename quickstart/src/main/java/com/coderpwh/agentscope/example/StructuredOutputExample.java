package com.coderpwh.agentscope.example;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.agent.Event;
import io.agentscope.core.agent.StreamOptions;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.message.TextBlock;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.List;

/**
 * @author coderpwh
 */
public class StructuredOutputExample {


    public static void main(String[] args) throws IOException {

        ExampleUtils.printWelcome("结构化输出", "此示例演示如何从智能体生成结构化输出。该智能体将分析用户查询并返回结构化数据。");

        String apiKey = ExampleUtils.getDashScopeApiKey();

    }

    private static void runProductAnalysisExample(ReActAgent agent) {
        String query = "我在找一台笔记本电脑。我需要至少 16GB 内存，偏好苹果品牌，预算大约在 2000 美元左右。它应该轻便，方便出行携带";
        System.out.println("query:" + query);

        Msg userMsg = Msg.builder()
                .role(MsgRole.USER)
                .content(TextBlock.builder().text("从该查询中提取产品需求:" + query).build())
                .build();

        try {
            Msg msg = agent.call(userMsg, ProductRequirements.class).block();

            ProductRequirements result = msg.getStructuredData(ProductRequirements.class);

            System.out.println("提取的结构化数据：");
            System.out.println("  产品类型：" + result.productType);
            System.out.println("  品牌：" + result.brand);
            System.out.println("  最小内存：" + result.minRam + " GB");
            System.out.println("  最高预算：$" + result.maxBudget);
            System.out.println("  功能特性：" + result.features);
        } catch (Exception e) {
            System.out.println("发生错误：" + e.getMessage());
            e.printStackTrace();
        }

    }


    private static void runContactExtractionExample(ReActAgent agent) {
        String query = "请联系 John Smith，邮箱：john.smith@example.com，或致电：+1-555-123-4567。他所在的公司是 TechCorp Inc";

        System.out.println("文本内容为: " + query);
        System.out.println();

        Msg userMsg = Msg.builder()
                .role(MsgRole.USER)
                .content(TextBlock.builder().text("从该查询中提取联系人信息:" + query).build())
                .build();


        try {
            Msg msg = agent.call(userMsg, ContactInfo.class).block();
            ContactInfo result = msg.getStructuredData(ContactInfo.class);

            System.out.println("提取的结构化数据：");
            System.out.println("  姓名：" + result.name);
            System.out.println("  邮箱：" + result.email);
            System.out.println("  电话：" + result.phone);
            System.out.println("  公司：" + result.company);
        } catch (Exception e) {
            System.out.println("发生错误：" + e.getMessage());
            e.printStackTrace();
        }
    }


    private static void runSentimentAnalysisExample(ReActAgent agent) {
        String review = "这款产品超出了我的预期！质量非常出色，客户服务也很有帮助。不过，发货时间比预期的要长一些";
        System.out.println("Review:" + review);

        Msg userMsg = Msg.builder()
                .role(MsgRole.USER)
                .content(TextBlock.builder().text("从该评论中提取产品需求:" + review).build()).build();

        try {

            Msg msg = agent.call(userMsg, SentimentAnalysis.class).block();
            SentimentAnalysis result = msg.getStructuredData(SentimentAnalysis.class);

            System.out.println("分析的结果为:");
            System.out.println("  总体评价：" + result.overallSentiment);
            System.out.println("  积极评分：" + result.positiveScore);
            System.out.println("  消极评分：" + result.negativeScore);
            System.out.println("  中立评分：" + result.neutralScore);
            System.out.println("  关键主题：" + result.keyTopics);
            System.out.println("  总结：" + result.summary);
        } catch (Exception e) {
            System.out.println("发生错误：" + e.getMessage());
            e.printStackTrace();
        }
    }


    private static void runStreamProductAnalysisExample(ReActAgent agent) {
        String query = "我在找一台笔记本电脑。我需要至少 16GB 内存，偏好苹果品牌，预算大约在 2000 美元左右。它应该轻便，适合出行携带";
        System.out.println("query:" + query);

        Msg userMsg = Msg.builder()
                .role(MsgRole.USER)
                .content(TextBlock.builder().text("从该查询中提取产品需求:" + query).build()).build();

        try {
            Flux<Event> eventFlux =
                    agent.stream(userMsg, StreamOptions.defaults(), ProductRequirements.class);
            ProductRequirements result = eventFlux.blockLast().getMessage().getStructuredData(ProductRequirements.class);

            System.out.println("提取的结构化数据：");
            System.out.println("  产品类型：" + result.productType);
            System.out.println("  品牌：" + result.brand);
            System.out.println("  最小内存：" + result.minRam + " GB");
            System.out.println("  最高预算：$" + result.maxBudget);
            System.out.println("  功能特性：" + result.features);
        } catch (Exception e) {
            System.out.println("发生错误：" + e.getMessage());
            e.printStackTrace();
        }


    }


    public static class ProductRequirements {

        public String productType;

        public String brand;

        public Integer minRam;

        public Double maxBudget;

        public List<String> features;

        public ProductRequirements() {
        }


    }


    public static class ContactInfo {
        public String name;

        public String email;

        public String phone;

        public String company;

        public ContactInfo() {
        }

    }

    public static class SentimentAnalysis {
        public String overallSentiment;
        public Double positiveScore;
        public Double negativeScore;
        public Double neutralScore;
        public List<String> keyTopics;
        public String summary;

        public SentimentAnalysis() {
        }

    }


}
