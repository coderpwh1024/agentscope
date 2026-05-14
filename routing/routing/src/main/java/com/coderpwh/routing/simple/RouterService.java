package com.coderpwh.routing.simple;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RouterService {

     private  static final Logger log = LoggerFactory.getLogger(RouterService.class);

     private  static  final String[] OUTPUT_KEYS = {"github_key", "notion_key", "slack_key"};

    private static final String SYNTHESIZE_SYSTEM_TEMPLATE =
            """
            Synthesize these search results to answer the original question: "%s"

            - Combine information from multiple sources without redundancy
            - Highlight the most relevant and actionable information
            - Note any discrepancies between sources
            - Keep the response concise and well-organized
            """;


}
