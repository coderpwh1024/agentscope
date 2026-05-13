package com.coderpwh.routing.simple.tools;

import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class NotionStubTools {
    @Tool(name = "search_notion", description = "Search Notion workspace for documentation.")
    public String searchNotion(
            @ToolParam(name = "query", description = "Search query") String query) {
        return "Found documentation: 'API Authentication Guide' - covers OAuth2 flow, API keys, and"
                + " JWT tokens";
    }

    @Tool(name = "get_page", description = "Get a specific Notion page by ID.")
    public String getPage(
            @ToolParam(name = "pageId", description = "Notion page ID") String pageId) {
        return "Page content: Step-by-step authentication setup instructions";
    }


}
