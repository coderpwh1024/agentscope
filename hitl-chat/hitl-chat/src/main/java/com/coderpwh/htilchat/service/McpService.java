package com.coderpwh.htilchat.service;

import com.coderpwh.htilchat.dto.McpConfigRequest;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.core.tool.mcp.McpClientBuilder;
import io.agentscope.core.tool.mcp.McpClientWrapper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author coderpwh
 */
@Service
public class McpService {
    private final Map<String, McpClientWrapper> mcpClients = new ConcurrentHashMap<>();


    /***
     * 添加 mcp 服务
     * @param request
     * @param toolkit
     * @return
     */

    public Mono<Void> addMcpServer(McpConfigRequest request, Toolkit toolkit) {

        String name = request.getName();
        if (mcpClients.containsKey(name)) {
            return Mono.error(new IllegalArgumentException("MCP server already exists: " + name));
        }

        return buildMcpClient(request)
                .flatMap(
                        client -> {
                            mcpClients.put(name, client);
                            return toolkit.registerMcpClient(client);
                        });
    }


    /**
     * 移除 mcp 服务
     *
     * @param name
     * @param toolkit
     * @return
     */
    public Mono<Void> removeMcpServer(String name, Toolkit toolkit) {
        McpClientWrapper mcpClient = mcpClients.remove(name);
        if (mcpClient != null) {
            return Mono.error(new IllegalArgumentException("MCP server not found: " + name));
        }

        return toolkit.removeMcpClient(name);
    }


    /**
     * 获取 mcp 服务列表
     *
     * @return
     */
    public List<String> listMcpServers() {
        return new ArrayList<>(mcpClients.keySet());
    }


    /***
     * 构建 mcp 客户端
     * @param request
     * @return
     */

    private Mono<McpClientWrapper> buildMcpClient(McpConfigRequest request) {

        McpClientBuilder builder = McpClientBuilder.create(request.getName());

        String transportType = request.getTransportType().toUpperCase();
        switch (transportType) {
            case "STDIO":
                String command = request.getCommand();
                List<String> args = request.getArgs();
                if (args == null || args.isEmpty()) {
                    builder.stdioTransport(command);
                } else {
                    builder.stdioTransport(command, args.toArray(new String[0]));
                }
                break;

            case "SSE":
                builder.sseTransport(request.getUrl());
                addHeadersAndParams(builder, request);
                break;

            case "HTTP":
                builder.streamableHttpTransport(request.getUrl());
                addHeadersAndParams(builder, request);
                break;

            default:
                return Mono.error(
                        new IllegalArgumentException("Unknown transport type: " + transportType));
        }

        return builder.buildAsync();
    }


    /**
     * 添加 mcp 头信息
     *
     * @param builder
     * @param request
     */
    private void addHeadersAndParams(McpClientBuilder builder, McpConfigRequest request) {

        Map<String, String> headers = request.getHeaders();

        if (headers != null) {
            headers.forEach(builder::header);
        }


        Map<String, String> queryParams = request.getQueryParams();
        if (queryParams != null) {
            queryParams.forEach(builder::queryParam);
        }

    }


}
