package com.coderpwh.agentscope.example;

import io.agentscope.core.tool.Toolkit;
import io.agentscope.core.tool.mcp.McpClientBuilder;
import io.agentscope.core.tool.mcp.McpClientWrapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author coderpwh
 */
public class McpToolExample {


    private static final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    public static void main(String[] args) throws Exception {

        ExampleUtils.printWelcome(
                "MCP Tool Example",
                "This example demonstrates MCP (Model Context Protocol) integration.\n"
                        + "MCP allows agents to use external tool servers like filesystem, git,"
                        + " databases, etc.");

        String apiKey = ExampleUtils.getDashScopeApiKey();

        McpClientWrapper mcpClient = configureMcp();

        Toolkit toolkit = new Toolkit();
        System.out.print("工具注册");
        toolkit.registerMcpClient(mcpClient).block();
        System.out.println("工具已注册");


    }

    private static McpClientWrapper configureMcp() throws Exception {
        System.out.println("选择MCP 转化类型");

        System.out.println(" 1) StdIO ");
        System.out.println(" 2) SSE ");
        System.out.println(" 3) HTTP ");

        String choice = reader.readLine().trim();

        switch (choice) {
            case "1":
                return configureStdioMcp();
            case "2":
                return configureSseMcp();
            case "3":
                return configureHttpMcp();
            default:
                System.out.println("初始化");
                return configureStdioMcp();
        }

    }

    private static McpClientWrapper configureSseMcp() throws Exception {
        System.out.println("\n --- SSE Configuration ---\n");

        System.out.print("Server URL:");
        String url = reader.readLine().trim();

        if (url.isEmpty()) {
            System.err.println("Error:URL required for SSE transport");
            return configureStdioMcp();
        }

        McpClientBuilder builder = McpClientBuilder.create("mcp")
                .sseTransport(url);

        System.out.println("是否添加授权");
        if (reader.readLine().trim().equalsIgnoreCase("y")) {
            System.out.print("Token:");
            String token = reader.readLine().trim();
            builder.header("Authorization", "Bearer " + token);
        }
        configureQueryParams(builder);

        return buildAndConnect(builder);

    }


    private static McpClientWrapper configureHttpMcp() throws Exception {
        System.out.println("\n--- HTTP Configureation ---\n");
        System.out.println("Server URL: ");

        String url = reader.readLine().trim();
        if (url.isEmpty()) {
            System.err.println("Error:URL required for HTTP transport");
            return configureStdioMcp();
        }

        McpClientBuilder builder = McpClientBuilder.create("mcp")
                .streamableHttpTransport(url);

        System.out.println("添加API  key header");
        if (reader.readLine().trim().equalsIgnoreCase("y")) {
            System.out.print("API Key: ");
            String apiKey = reader.readLine().trim();
            builder.header("x-api-key", apiKey);
        }

        configureQueryParams(builder);

        return buildAndConnect(builder);
    }

    private static McpClientWrapper configureStdioMcp() throws IOException {
        System.out.println("\n --- StdIO Configuration ---\n");

        System.out.print("命令执行");

        String command = reader.readLine().trim();
        if (command.isEmpty()) {
            command = "npx";
        }

        System.out.println("\n Common MCP servers:");
        System.out.println("1) 文件系统 -访问文件");
        System.out.println("2) 浏览器 -访问网页");
        System.out.println("3) 聊天 -对话");
        System.out.println("完成记录");

        String serverChoice = reader.readLine().trim();
        String[] mcpArgs;

        switch (serverChoice) {
            case "1":
                System.out.print("Directory path");
                String path = reader.readLine().trim();
                if (path.isEmpty()) {
                    path = "/tmp";
                }

                mcpArgs = new String[]{"-y", "@modelcontextprotocol/server-fileystem", path};
                break;
            case "2":
                mcpArgs = new String[]{"-y", "@modelcontextprotocol/server-git"};
                break;

            default:
                System.out.print("参数");
                String argsStr = reader.readLine().trim();
                if (argsStr.isEmpty()) {
                    mcpArgs = new String[]{"-y", "@modelcontextprotocol/server-chat"};
                } else {
                    mcpArgs = argsStr.split(",");
                }
        }
        System.out.println("连接MCP 服务");

        try {
            McpClientWrapper client = McpClientBuilder
                    .create("mcp")
                    .stdioTransport(command, mcpArgs)
                    .buildAsync()
                    .block();

            System.out.println(" Connected!\n");
            return client;
        } catch (Exception e) {
            System.err.println("链接失败");
            System.err.println("Error: " + e.getMessage());
            System.err.println("MCP 服务正在安装");
            System.err.println(" 安装模型服务中");
            throw e;
        }


    }


    private static void configureQueryParams(McpClientBuilder builder) throws IOException {
        System.out.println("添加查询参数?");

        if (!reader.readLine().trim().equalsIgnoreCase("y")) {
            return;
        }
        System.out.println("请输入查询参数（格式：key=value，空行结束）:");
        while (true) {
            System.out.println(" > ");
            String param = reader.readLine().trim();
            if (param.isEmpty()) {
                break;
            }
            String[] parts = param.split("=", 2);
            if (parts.length == 2) {
                builder.queryParam(parts[0].trim(), parts[1].trim());
            } else {
                System.out.println("参数格式错误");
            }

        }

    }


    private static McpClientWrapper buildAndConnect(McpClientBuilder builder) throws Exception {
        System.out.println("连接MCP服务");
        try {
            McpClientWrapper client = builder.buildAsync().block();
            System.out.println("MCP服务已连接");
            return client;
        } catch (Exception e) {
            System.err.println("链接失败");
            System.err.println("Error: " + e.getMessage());
            throw e;
        }

    }


}
