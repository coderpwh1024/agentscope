package com.coderpwh.htilchat.service;

import io.agentscope.core.tool.mcp.McpClientWrapper;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author coderpwh
 */
@Service
public class McpService {
    private final Map<String, McpClientWrapper> mcpClients = new ConcurrentHashMap<>();


}
