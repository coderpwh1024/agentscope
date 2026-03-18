package com.coderpwh.agentscope.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author coderpwh
 */
public class SessionExample {


    private static final String DEFAULT_SESSION_ID = "default_session";


    private static final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    public static void main(String[] args) throws IOException {

        ExampleUtils.printWelcome(
                "Session Example",
                "此示例演示持久化会话功能。\n"
                        + "您的对话将被保存，并可在稍后恢复。");

        String apiKey = ExampleUtils.getDashScopeApiKey();


    }

    private static String getSessionId() throws Exception {
        System.out.println("请输入会话ID（回车使用默认会话）：");
        String sessionId = reader.readLine().trim();

        if (sessionId.isEmpty()) {
            sessionId = DEFAULT_SESSION_ID;
        }
        System.out.println("Using session:"+sessionId);
        return sessionId;
    }


}
