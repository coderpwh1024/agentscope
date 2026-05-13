package com.coderpwh.routing.graph.tools;

import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class GitHubStubTools {


    @Tool(name = "search_code", description = "Search code in Github repositories.")
    public String searchCode(@ToolParam(name = "query", description = "Search query") String query, @ToolParam(name = "repo", description = "Repository name") String repo) {
        String rep = repo != null ? repo : "main";
        return "Found code matching '"
                + query
                + "' in "
                + rep
                + ": authentication middleware in src/auth.py";
    }

    @Tool(name = "search_issues", description = "Search Github issues and pull request.")
    public String searchIssues(@ToolParam(name = "query", description = "Search query") String query) {
        return "Found 3 issues matching '"
                + query
                + "': #142 (API auth docs), #89 (OAuth flow), #2" +
                "03 (token refresh)";
    }

}
