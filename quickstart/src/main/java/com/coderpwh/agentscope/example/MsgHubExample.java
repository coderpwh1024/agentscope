package com.coderpwh.agentscope.example;

import com.coderpwh.agentscope.util.MsgUtils;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.formatter.dashscope.DashScopeChatFormatter;
import io.agentscope.core.memory.InMemoryMemory;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.pipeline.MsgHub;
import io.agentscope.core.tool.Toolkit;

import java.io.IOException;

/**
 * @author coderpwh
 */
public class MsgHubExample {


    public static void main(String[] args) throws IOException {

        ExampleUtils.printWelcome(
                "MsgHub 示例 - 多 Agent 对话",
                "本示例演示如何使用 MsgHub 实现多 Agent 之间的对话。\n"
                        + "三位学生（Alice、Bob 和 Charlie）将共同讨论一个话题。\n"
                        + "MsgHub 会自动将每位学生的发言广播给其他人。");

        String apikey = ExampleUtils.getDashScopeApiKey();

        DashScopeChatModel model = DashScopeChatModel
                .builder()
                .apiKey(apikey)
                .modelName("qwen-plus")
                .formatter(new DashScopeChatFormatter())
                .build();

        System.out.println("\n=== Creating Three Student Agents ===\n");

        ReActAgent alice = ReActAgent
                .builder()
                .name("Alice")
                .sysPrompt("""
                        你是 Alice,一个总是积极乐观、善于发现事物美好一面的学生,回答要简短（1-2句话），并保持热情洋溢的风格
                        """)
                .model(model)
                .memory(new InMemoryMemory())
                .toolkit(new Toolkit())
                .build();

        ReActAgent bob = ReActAgent
                .builder()
                .name("Bob")
                .sysPrompt("""
                        你是 Bob,一个注重实际,关注现实问题的务实学生,回答要简短（1-2句话）,并保持实事求是的风格"
                         """)
                .model(model)
                .memory(new InMemoryMemory())
                .toolkit(new Toolkit())
                .build();


        ReActAgent charlie = ReActAgent
                .builder()
                .name("Charlie")
                .sysPrompt("你是查理，一个思维不拘一格的创意学生。回答要简洁（1-2句话），并富有想象力")
                .model(model)
                .memory(new InMemoryMemory())
                .toolkit(new Toolkit())
                .build();

        System.out.println("Created agents: Alice (Optimistic), Bob (Pragmatic), Charlie" + " (Creative)\n");


        basicConversationExample(alice, bob, charlie);
        reactiveConversationExample(alice, bob, charlie);

        System.out.println("\n");
        System.out.println("\n=== MsgHub demo 已完成 ===\n");

        System.out.println(
                "\n核心要点："
                        + "\n1. MsgHub 实现了智能体之间消息广播的自动化"
                        + "\n2. 使用 block() 实现同步风格的代码"
                        + "\n3. 使用 then() 实现完全响应式的代码"
                        + "\n4. 每个智能体维护自己的对话记忆");

    }

    private static void basicConversationExample(ReActAgent alice, ReActAgent bob, ReActAgent charlie) {
        System.out.println("\n=== Basic Conversation Example ===\n");

        Msg announcement = Msg
                .builder()
                .name("system")
                .role(MsgRole.SYSTEM)
                .content(TextBlock.builder().text("我们来讨论：学习一门新编程语言的最佳方式是什么？每人分享一个简短的想法").build())
                .build();

        try (MsgHub hub = MsgHub
                .builder()
                .name("StudentDiscussion")
                .participants(alice, bob, charlie)
                .announcement(announcement)
                .enableAutoBroadcast(true)
                .build()) {

            hub.enter().block();

            System.out.println("宣告:" + MsgUtils.getTextContent(announcement) + "\n");
            System.out.println("[Alices turn]");
            Msg aliceResponse = alice.call().block();
            printAgentResponse("Alice", aliceResponse);


            System.out.println("\n[Charlie's turn]");
            Msg charlieResponse = charlie.call().block();
            printAgentResponse("Charlie", charlieResponse);

            System.out.println("\n--- 记忆验证 ----");
            System.out.println("Alice 的记忆：" + alice.getMemory().getMessages().size() + " messages");
            System.out.println("Bob 的记忆:" + bob.getMemory().getMessages().size() + " messages");
            System.out.println("Charlie 的记忆:" + charlie.getMemory().getMessages().size() + " messages");

            System.out.println("这是所有的记忆");
        }
    }


    private static void reactiveConversationExample(ReActAgent alice, ReActAgent bob, ReActAgent charlie) {
        System.out.println();

        alice.getMemory().clear();
        bob.getMemory().clear();
        charlie.getMemory().clear();


        Msg announcement = Msg
                .builder()
                .name("system")
                .role(MsgRole.USER)
                .content(TextBlock.builder().text("Quick question: What's your favorite programming paradigm").build())
                .build();


        MsgHub hub = MsgHub
                .builder()
                .name("ReactiveDiscussion")
                .participants(alice, bob, charlie)
                .announcement(announcement)
                .build();

        hub.enter()
                .doOnSuccess(h -> System.out.println("公告:" + MsgUtils.getTextContent(announcement) + "\n"))
                .then(alice.call())
                .doOnSuccess(msg -> System.out.println("[Alice]:" + MsgUtils.getTextContent(msg)))
                .then(bob.call())
                .doOnSuccess(msg -> System.out.println("[Bob]:" + MsgUtils.getTextContent(msg)))
                .then(charlie.call())
                .doOnSuccess(msg -> System.out.println("[Charlie]:" + MsgUtils.getTextContent(msg)))
                .then(hub.exit())
                .doOnSuccess(v -> System.out.println("响应式链已完成"))
                .block();


    }


    private static void printAgentResponse(String name, Msg msg) {
        String content = MsgUtils.getTextContent(msg);
        if (content != null && !content.isEmpty()) {
            System.out.println(name + ": " + content);
        }

    }


}
