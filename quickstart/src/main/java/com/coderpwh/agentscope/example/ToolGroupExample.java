package com.coderpwh.agentscope.example;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import io.agentscope.core.tool.Toolkit;

public class ToolGroupExample {

    public static void main(String[] args) throws IOException {
        ExampleUtils.printWelcome(
                "工具组示例 - 元工具演示",
                "此示例演示智能体自主管理工具组的能力。\n"
                        + "智能体可以使用 reset_equipped_tools 元工具来激活工具组。");

        String apiKey = ExampleUtils.getDashScopeApiKey();


    }

    private static Toolkit configureToolkit() {
        Toolkit toolkit = new Toolkit();
        toolkit.createToolGroup("file_ops", "File system operations (read,write,list)", false);
        toolkit.registration().tool(new FileTools()).group("file_ops").apply();

        // TODO
        return  null;

    }

    public static class FileTools {

        @Tool(name = "read_file", description = "Read contents of a file")
        public String readFile(@ToolParam(name = "path", description = "File path to read") String path) {
            try {
                Path filePath = Paths.get(path);
                if (!Files.exists(filePath)) {
                    return "Error:File not found:" + path;
                }
                return Files.readString(filePath);
            } catch (Exception e) {
                return "Error reading file:" + e.getMessage();
            }
        }

        @Tool(name = "write_file", description = "Write contents to a file")
        public String writeFile(@ToolParam(name="path",description = "File path to write") String path,
                                @ToolParam(name="content",description = "Content to write") String content) {

            try {
                Files.writeString(Paths.get(path),content);
                return "Successfully wrote to"+path;
            }catch (Exception e){
                return "Error writing file:" + e.getMessage();
            }

        }


    }

}
