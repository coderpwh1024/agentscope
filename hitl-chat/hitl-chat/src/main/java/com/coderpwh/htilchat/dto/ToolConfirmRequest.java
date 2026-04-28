package com.coderpwh.htilchat.dto;

import java.util.List;

/**
 * @author coderpwh
 */
public class ToolConfirmRequest {

    private String sessionId;
    private boolean confirmed;
    private String reason;
    private List<ToolCallInfo> toolCalls;

    public ToolConfirmRequest() {}

    public ToolConfirmRequest(
            String sessionId, boolean confirmed, String reason, List<ToolCallInfo> toolCalls) {
        this.sessionId = sessionId;
        this.confirmed = confirmed;
        this.reason = reason;
        this.toolCalls = toolCalls;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public List<ToolCallInfo> getToolCalls() {
        return toolCalls;
    }

    public void setToolCalls(List<ToolCallInfo> toolCalls) {
        this.toolCalls = toolCalls;
    }

    /** Tool call information for rejection response. */
    public static class ToolCallInfo {
        private String id;
        private String name;

        public ToolCallInfo() {}

        public ToolCallInfo(String id, String name) {
            this.id = id;
            this.name = name;
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
    }
}
