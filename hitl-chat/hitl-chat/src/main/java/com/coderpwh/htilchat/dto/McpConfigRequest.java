package com.coderpwh.htilchat.dto;

import java.util.List;
import java.util.Map;

/**
 * @author coderpwh
 */
public class McpConfigRequest {

    /** MCP server name (unique identifier). */
    private String name;

    /** Transport type: STDIO, SSE, HTTP. */
    private String transportType;

    /** For STDIO: command to execute. */
    private String command;

    /** For STDIO: command arguments. */
    private List<String> args;

    /** For SSE/HTTP: server URL. */
    private String url;

    /** HTTP headers for SSE/HTTP transport. */
    private Map<String, String> headers;

    /** Query parameters for SSE/HTTP transport. */
    private Map<String, String> queryParams;

    public McpConfigRequest() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTransportType() {
        return transportType;
    }

    public void setTransportType(String transportType) {
        this.transportType = transportType;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public List<String> getArgs() {
        return args;
    }

    public void setArgs(List<String> args) {
        this.args = args;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public Map<String, String> getQueryParams() {
        return queryParams;
    }

    public void setQueryParams(Map<String, String> queryParams) {
        this.queryParams = queryParams;
    }
}
