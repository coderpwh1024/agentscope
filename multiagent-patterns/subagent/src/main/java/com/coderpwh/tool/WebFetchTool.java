package com.coderpwh.tool;

import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * 抓取网页工具类
 *
 * @author coderpwh
 */
public class WebFetchTool {


    /**
     * 抓取网页
     * @param url
     * @param prompt
     * @return
     */
    @Tool(name="web_fetch",description = "Fetch content from a URL. Use for documentation, research, or comparing\"\n" +
            "                            + \" technologies. Returns raw text (e.g. HTML as text).")
    public String webFetch(@ToolParam(name="prompt",description ="Full URL to fetch (e.g. https://example.com)") String url,
                          @ToolParam(name = "prompt",description ="Optional prompt describing what to extract or summarize",required = false) String prompt) {

        if (url == null || url.isBlank()) {
            return "Error: url is required.";
        }
        try {
            URI.create(url);
            URL u = new URL(url);
            try (InputStream in = u.openStream();
                 Scanner scanner =
                         new Scanner(in, StandardCharsets.UTF_8.name()).useDelimiter("\\A")) {
                String content = scanner.hasNext() ? scanner.next() : "";
                if (content.length() > 15000) {
                    content = content.substring(0, 15000) + "\n...[truncated]";
                }
                return content;
            }
        } catch (Exception e) {
            return "Error fetching URL: " + e.getMessage();
        }
    }

}
