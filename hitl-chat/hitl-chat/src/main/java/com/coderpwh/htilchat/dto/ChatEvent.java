package com.coderpwh.htilchat.dto;

import java.util.List;
import java.util.Map;

/**
 * @author coderpwh
 */
public class ChatEvent {
    /**
     * Event type: TEXT, TOOL_USE, TOOL_RESULT, TOOL_CONFIRM, ERROR, COMPLETE.
     */
    private String type;

    /**
     * Text content for TEXT events.
     */
    private String content;

    /**
     * Tool name for TOOL_USE/TOOL_RESULT events.
     */
    private String toolName;

    /**
     * Tool ID for TOOL_USE/TOOL_RESULT events.
     */
    private String toolId;

    /**
     * Tool input parameters for TOOL_USE events.
     */
    private Map<String, Object> toolInput;

    /**
     * Tool result for TOOL_RESULT events.
     */
    private String toolResult;

    /**
     * Pending tool calls for TOOL_CONFIRM events.
     */
    private List<PendingToolCall> pendingToolCalls;

    /**
     * Error message for ERROR events.
     */
    private String error;

    /**
     * Indicates if this is incremental content.
     */
    private boolean incremental;

    public ChatEvent() {
    }

    public static ChatEvent text(String content, boolean incremental) {
        ChatEvent event = new ChatEvent();
        event.type = "TEXT";
        event.content = content;
        event.incremental = incremental;
        return event;
    }

    public static ChatEvent toolUse(String toolId, String toolName, Map<String, Object> input) {
        ChatEvent event = new ChatEvent();
        event.type = "TOOL_USE";
        event.toolId = toolId;
        event.toolName = toolName;
        event.toolInput = input;
        return event;
    }

    public static ChatEvent toolResult(String toolId, String toolName, String result) {
        ChatEvent event = new ChatEvent();
        event.type = "TOOL_RESULT";
        event.toolId = toolId;
        event.toolName = toolName;
        event.toolResult = result;
        return event;
    }

    public static ChatEvent toolConfirm(List<PendingToolCall> pendingToolCalls) {
        ChatEvent event = new ChatEvent();
        event.type = "TOOL_CONFIRM";
        event.pendingToolCalls = pendingToolCalls;
        return event;
    }

    public static ChatEvent error(String error) {
        ChatEvent event = new ChatEvent();
        event.type = "ERROR";
        event.error = error;
        return event;
    }

    public static ChatEvent complete() {
        ChatEvent event = new ChatEvent();
        event.type = "COMPLETE";
        return event;
    }

    public static ChatEvent interrupted(String message) {
        ChatEvent event = new ChatEvent();
        event.type = "INTERRUPTED";
        event.content = message;
        return event;
    }

    // Getters and setters

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getToolName() {
        return toolName;
    }

    public void setToolName(String toolName) {
        this.toolName = toolName;
    }

    public String getToolId() {
        return toolId;
    }

    public void setToolId(String toolId) {
        this.toolId = toolId;
    }

    public Map<String, Object> getToolInput() {
        return toolInput;
    }

    public void setToolInput(Map<String, Object> toolInput) {
        this.toolInput = toolInput;
    }

    public String getToolResult() {
        return toolResult;
    }

    public void setToolResult(String toolResult) {
        this.toolResult = toolResult;
    }

    public List<PendingToolCall> getPendingToolCalls() {
        return pendingToolCalls;
    }

    public void setPendingToolCalls(List<PendingToolCall> pendingToolCalls) {
        this.pendingToolCalls = pendingToolCalls;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public boolean isIncremental() {
        return incremental;
    }

    public void setIncremental(boolean incremental) {
        this.incremental = incremental;
    }

    /**
     * Pending tool call information for confirmation.
     */
    public static class PendingToolCall {

        private String id;
        private String name;
        private Map<String, Object> input;
        private boolean dangerous;

        public PendingToolCall() {
        }

        public PendingToolCall(
                String id, String name, Map<String, Object> input, boolean dangerous) {
            this.id = id;
            this.name = name;
            this.input = input;
            this.dangerous = dangerous;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Map<String, Object> getInput() {
            return input;
        }

        public void setInput(Map<String, Object> input) {
            this.input = input;
        }

        public boolean isDangerous() {
            return dangerous;
        }

        public void setDangerous(boolean dangerous) {
            this.dangerous = dangerous;
        }
    }
}
