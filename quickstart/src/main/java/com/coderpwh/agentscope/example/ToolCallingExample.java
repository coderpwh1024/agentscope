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
        toolkit.registerTool(new);

        System.out.println("已注册的工具：");
        System.out.println("  - get_current_time: 获取指定时区的当前时间");
        System.out.println("  - calculate: 计算简单的数学表达式");
        System.out.println("  - search: 模拟搜索功能\n");
    }


    public static class SimpleTools {
        @Tool(name = "get_current_time", description = "Get the current time in a specific timezone")
        public String getCurrentTime(@ToolParam(name = "timezone", description = "Timezone name, e.g., 'Asia/Tokyo', 'America/New_York','Europe/London'") String timezone) {


            ZoneId zoneId = ZoneId.of(timezone);
            LocalDateTime now = LocalDateTime.now(zoneId);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return String.format("The current time in %s is %s", timezone, now.format(formatter));

        }

    }


}
