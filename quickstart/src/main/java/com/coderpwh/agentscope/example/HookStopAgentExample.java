package com.coderpwh.agentscope.example;

import java.awt.*;
import java.io.IOException;

import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import io.agentscope.core.tool.Toolkit;

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

        String apiKey = ExampleUtils.getDashScopeApiKey();

        Toolkit toolkit = new Toolkit();


    }

    public static class SensitiveTools {
        @Tool(name = "deleteFile", description = "Delete a file from the system")
        public String deleteFile(@ToolParam(name = "filename", description = "Name of the file to delete") String filename) {
            System.out.println("工具 删除文件:"+filename);
            return "File '" + filename + "' has been deleted successfully.";
        }
    }



}
