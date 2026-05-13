package com.coderpwh.routing.graph.tools;

import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import org.springframework.stereotype.Component;


@Component
public class NotionStubTools {


    @Tool(name = "search_notion", description = "Search notion workspace for documentation")
    public String searchNotion(@ToolParam(name = "query", description = "Search query") String query) {
        return "Found documentation: 'API Authentication Guide' - covers OAuth2 flow, API keys, and"
                + " JWT tokens";
    }

    @Tool(name = "get_page", description = "Get a specific Notion page By Id.")
    public String getPage(@ToolParam(name = "pageId", description = "Notion page Id") String pageId) {
        return "Page content: Step-by-step authentication setup instructions";
    }


}
