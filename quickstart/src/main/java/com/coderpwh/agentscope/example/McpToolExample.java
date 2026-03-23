package com.coderpwh.agentscope.example;

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

    public static void main(String[] args) {

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


    private static McpClientWrapper buildAndContent(McpClientBuilder builder) throws Exception {
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
