//package com.coderpwh.routing.graph.tools;
//
//import io.agentscope.core.tool.Tool;
//import io.agentscope.core.tool.ToolParam;
//import org.springframework.stereotype.Component;
//
//@Component
//public class SlackStubTools {
//
//
//    @Tool(name = "search_slack", description = "Search Slack messages and threads")
//    public String searchSlack(@ToolParam(name = "query", description = "Search query") String query) {
//        return "Found discussion in #engineering: 'Use Bearer tokens for API auth, see docs for"
//                + " refresh flow'";
//    }
//
//    @Tool(name = "get_thread", description = "Get a specific Slack thread.")
//    public String getThread(@ToolParam(name = "threadId", description = "Slack thread ID") String threadId) {
//        return "Thread discusses best practices for API key rotation";
//    }
//
//}
