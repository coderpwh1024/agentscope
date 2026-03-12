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
        return null;

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
        public String writeFile(@ToolParam(name = "path", description = "File path to write") String path,
                                @ToolParam(name = "content", description = "Content to write") String content) {

            try {
                Files.writeString(Paths.get(path), content);
                return "Successfully wrote to" + path;
            } catch (Exception e) {
                return "Error writing file:" + e.getMessage();
            }

        }


        @Tool(name = "list_files", description = "List files in a directory")
        public String listFiles(@ToolParam(name = "directory", description = "Directory path") String directory) {
            try {
                Path dir = Paths.get(directory);
                if (!Files.isDirectory(dir)) {
                    return "Error:Not a directory:" + directory;
                }
                StringBuilder result = new StringBuilder("Files in " + directory + ":\n");
                Files.list(dir).forEach(path -> result.append(" -" + path.getFileName().toString() + "\n"));
                return result.toString();
            } catch (Exception e) {
                return "Error listing directory: " + e.getMessage();
            }
        }
    }

    public static class MathTools {

        /**
         * 阶乘计算
         *
         * @param n
         * @return
         */
        @Tool(name = "factorial", description = "Calculate factorial of a number")
        public String factorial(@ToolParam(name = "n", description = "Number to calculate factorial") Integer n) {

            if (n < 0) {
                return "Error: Factorial not defined for negative numbers";
            }

            if (n > 20) {
                return "Error: Number too large (max 20)";
            }
            long result = 1;

            for (int i = 2; i <= n; i++) {
                result *= i;
            }

            return String.format("factorial(%d) = %d", n, result);
        }


        @Tool(name = "is_prime", description = "Check if a number is prime")
        public String isPrime(@ToolParam(name = "n", description = "Number to check") Integer n) {
            if (n < 2) {
                return n + "is not a prime number";
            }

            for (int i = 2; i <= Math.sqrt(n); i++) {
                if (n % i == 0) {
                    return n + "is not a prime number";
                }
            }
            return n + "is a prime number";
        }
    }





}
