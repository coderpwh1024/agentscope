package com.coderpwh.agentscope.example;

import java.io.IOException;

/**
 * @author coderpwh
 */
public class HookStopAgentExample {


    public static void main(String[] args) throws IOException {
        ExampleUtils.printWelcome(
                "Hook Stop Agent 示例",
                "此示例演示了人机协作的工具确认机制。\n"
                        + "代理在执行敏感操作前会暂停，\n"
                        + "让您可以查看并确认工具调用。");

        String  apiKey = ExampleUtils.getDashScopeApiKey();




    }


}
