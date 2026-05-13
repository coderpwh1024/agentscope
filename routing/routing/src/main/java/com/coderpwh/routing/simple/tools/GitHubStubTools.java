package com.coderpwh.routing.simple.tools;

import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class GitHubStubTools {


    @Tool(name = "search_code", description = "Search code in GitHub repositories.")
    public String searchCode(
            @ToolParam(name = "query", description = "Search query") String query,
            @ToolParam(name = "repo", description = "Repository name", required = false)
            String repo) {
        String rep = repo != null ? repo : "main";
        return "Found code matching '"
                + query
                + "' in "
                + rep
                + ": authentication middleware in src/auth.py";
    }

    @Tool(name = "search_issues", description = "Search GitHub issues and pull requests.")
    public String searchIssues(
            @ToolParam(name = "query", description = "Search query") String query) {
        return "Found 3 issues matching '"
                + query
                + "': #142 (API auth docs), #89 (OAuth flow), #203 (token refresh)";
    }

    @Tool(name = "search_prs", description = "Search pull requests for implementation details.")
    public String searchPrs(@ToolParam(name = "query", description = "Search query") String query) {
        return "PR #156 added JWT authentication, PR #178 updated OAuth scopes";
    }
}
