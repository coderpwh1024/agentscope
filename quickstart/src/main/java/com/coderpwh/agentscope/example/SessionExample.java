package com.coderpwh.agentscope.example;

import com.coderpwh.agentscope.util.MsgUtils;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.formatter.dashscope.DashScopeChatFormatter;
import io.agentscope.core.memory.InMemoryMemory;
import io.agentscope.core.memory.Memory;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.session.JsonSession;
import io.agentscope.core.session.Session;
import io.agentscope.core.state.SimpleSessionKey;
import io.agentscope.core.tool.Toolkit;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * @author coderpwh
 */
public class SessionExample {


    private static final String DEFAULT_SESSION_ID = "default_session";


    private static final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    public static void main(String[] args) throws Exception {

        ExampleUtils.printWelcome(
                "Session Example",
                "此示例演示持久化会话功能。\n"
                        + "您的对话将被保存，并可在稍后恢复。");

        String apiKey = ExampleUtils.getDashScopeApiKey();
        String sessionId = getSessionId();

        Path sessionPath =
                Paths.get(System.getProperty("user.home"), ".agentscope", "examples", "sessions");
        Session session = new JsonSession(sessionPath);


        InMemoryMemory memory = new InMemoryMemory();

        Toolkit toolkit = new Toolkit();

        ReActAgent agent = ReActAgent
                .builder()
                .name("Assistant")
                .sysPrompt("你是一个拥有持久记忆的 AI 助手。你能够记住来自之前对话中的信息")
                .toolkit(toolkit)
                .memory(memory)
                .model(DashScopeChatModel
                        .builder()
                        .apiKey(apiKey)
                        .modelName("qwen-max")
                        .stream(true)
                        .enableThinking(false)
                        .formatter(new DashScopeChatFormatter())
                        .build()).build();


        loadSession(agent, session, sessionId, memory);

        runConversation(agent, session, sessionId);

        saveSession(agent, session, sessionId);
    }

    private static String getSessionId() throws Exception {
        System.out.println("请输入会话ID（回车使用默认会话）：");
        String sessionId = reader.readLine().trim();

        if (sessionId.isEmpty()) {
            sessionId = DEFAULT_SESSION_ID;
        }
        System.out.println("Using session:" + sessionId);
        return sessionId;
    }

    private static void loadSession(ReActAgent agent, Session session, String sessionId, InMemoryMemory memory) {
        if (session.exists(SimpleSessionKey.of(sessionId))) {
            agent.loadFrom(session, sessionId);
            int messageCount = memory.getMessages().size();
            System.out.println("已加载:" + sessionId + messageCount + "条消息");
            if (messageCount > 0) {
                System.out.println("已加载:" + sessionId + messageCount + "条消息");
            }
        } else {
            System.out.println("未找到会话:" + sessionId);
        }
    }

    private static void runConversation(ReActAgent agent, Session session, String sessionId) throws IOException {
        System.out.println("开始");
        System.out.println("历史消息");
        while (true) {
            System.out.println("用户:");
            String input = reader.readLine();

            if (input == null || "exit".equalsIgnoreCase(input.trim())) {
                break;
            }

            if (input.trim().isEmpty()) {
                continue;
            }

            if ("history".equalsIgnoreCase(input.trim())) {
                showHistory(agent.getMemory());
                continue;
            }

            Msg userMsg = Msg.builder()
                    .role(MsgRole.USER)
                    .content(TextBlock.builder().text(input).build()).build();

            Msg response = agent.call(userMsg).block();

            if (response != null) {
                System.out.println("智能体:" + MsgUtils.getTextContent(response) + "\n");
            } else {
                System.out.println("智能体:" + "无回复");
            }

            agent.saveTo(session, sessionId);
        }

    }

    private static void saveSession(ReActAgent agent, Session session, String sessionId) {

        try {
            agent.saveTo(session, sessionId);
            System.out.println("会话保存:" + sessionId);
        } catch (Exception e) {
            System.err.println("保存会话失败:" + e.getMessage());

        }


    }


    private static void showHistory(Memory memory) {

        List<Msg> messages = memory.getMessages();
        if (messages.isEmpty()) {
            System.out.println("无历史消息");
            return;
        }

        System.out.println("历史消息");
        for (int i = 0; i < messages.size(); i++) {
            Msg msg = messages.get(i);
            String role = msg.getRole() == MsgRole.USER ? "用户" : "智能体";
            String content = MsgUtils.getTextContent(msg);

            if (content.length() > 100) {
                content = content.substring(0, 97) + "...";
            }
            System.out.println((i + 1) + ". " + role + ": " + content);
        }
        System.out.println("总共的消息:" + messages.size());
        System.out.println("============================结束============================");
        System.out.println();
    }


}
