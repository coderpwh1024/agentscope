package com.coderpwh.agentscope.example;

import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import io.agentscope.core.tool.Toolkit;

import java.io.IOException;
import java.text.DateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class ToolCallingExample {

    public static void main(String[] args) throws IOException {

        ExampleUtils.printWelcome(
                "工具调用示例",
                "此示例演示如何为智能体配置工具。\n"
                        + "该智能体可以使用：时间查询、计算器和搜索功能。");

        String apiKey = ExampleUtils.getDashScopeApiKey();

        Toolkit toolkit = new Toolkit();
        toolkit.registerTool();

        System.out.println("已注册的工具：");
        System.out.println("  - get_current_time: 获取指定时区的当前时间");
        System.out.println("  - calculate: 计算简单的数学表达式");
        System.out.println("  - search: 模拟搜索功能\n");
    }


    public static class SimpleTools {
        @Tool(name = "get_current_time", description = "Get the current time in a specific timezone")
        public String getCurrentTime(@ToolParam(name = "timezone", description = "Timezone name, e.g., 'Asia/Tokyo', 'America/New_York','Europe/London'") String timezone) {

            try {
                ZoneId zoneId = ZoneId.of(timezone);
                LocalDateTime now = LocalDateTime.now(zoneId);

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                return String.format("The current time in %s is %s", timezone, now.format(formatter));
            } catch (Exception e) {
                return "错误：无效的时区。请尝试 'Asia/Tokyo' 或 'America/New_York'";
            }
        }

        @Tool(name = "calculate", description = "Calculate simple math expressions")
        public String calculate(@ToolParam(name = "expression", description = "Math expression to evaluate,e.g. '123+456','10*20'") String expression) {
            try {
                expression = expression.replaceAll("\\s+", "");
                double result;
                if (expression.contains("+")) {
                    String[] parts = expression.split("\\+");
                    result = Double.parseDouble(parts[0]) + Double.parseDouble(parts[1]);
                } else if (expression.contains("-")) {
                    String[] parts = expression.split("-");
                    result = Double.parseDouble(parts[0]) - Double.parseDouble(parts[1]);
                } else if (expression.contains("*")) {
                    String[] parts = expression.split("\\*");
                    result = Double.parseDouble(parts[0]) * Double.parseDouble(parts[1]);
                } else if (expression.contains("/")) {
                    String[] parts = expression.split("/");
                    result = Double.parseDouble(parts[0]) / Double.parseDouble(parts[1]);
                } else {
                    return "Error: Unsupported operation. Use +, -, *, or /";
                }

                return String.format("%s = %.2f", expression, result);
            } catch (Exception e) {
                return "错误：表达式无效。示例：'123 + 456'";
            }

        }

        @Tool(name = "search", description = "Simulate a search function")
        public String search(@ToolParam(name = "query", description = "Search query") String query) {

            String lowerQuery = query.toLowerCase();
            if (lowerQuery.contains("ai") || lowerQuery.contains("artificial intelligence")) {
                return "关于'人工智能'的搜索结果：\n"
                        + "1. AI 是机器对人类智能的模拟\n"
                        + "2. 常见的 AI 应用：聊天机器人、图像识别、自动驾驶汽车\n"
                        + "3. 主要的 AI 技术：机器学习、深度学习、自然语言处理";
            } else if (lowerQuery.contains("java")) {
                return "关于'Java'的搜索结果：\n"
                        + "1. Java 是一种高级的面向对象编程语言\n"
                        + "2. 由 Sun Microsystems 公司于 1995 年首次发布\n"
                        + "3. 以'一次编写，到处运行'（WORA）理念而闻名";
            } else if (lowerQuery.contains("agentscope")) {
                return "关于'AgentScope'的搜索结果：\n"
                        + "1. AgentScope 是一个面向智能体的编程框架\n"
                        + "2. 支持使用多智能体系统构建大语言模型应用\n"
                        + "3. 提供透明且灵活的智能体开发方式";
            } else {
                return String.format(
                        "关于'%s'的搜索结果：\n"
                                + "1. 关于 %s 的结果（模拟）\n"
                                + "2. 更多关于 %s 的信息（模拟）\n"
                                + "3. 与 %s 相关的主题（模拟）",
                        query, query, query, query);
            }
        }


    }


}


}
