package com.coderpwh.advanced.hits;

import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolSuspendException;

import java.util.List;
import java.util.Map;

/**
 * @author coderpwh
 */
public class UserInteractionTool {


    public static final String TOOL_NAME = "ask_user";


    /**
     * 获取用户输入
     *
     * @return
     */
    @Tool(name = TOOL_NAME, description =
            "Ask the user for clarification or additional information when the request is"
                    + " ambiguous or missing required details. Choose the appropriate ui_type:"
                    + " 'text' for free-form input, 'select' for choosing one from a list"
                    + " (provide options), 'multi_select' for choosing multiple from a list,"
                    + " 'confirm' for yes/no questions, 'form' for collecting multiple fields"
                    + " at once (provide fields), 'date' for date selection, 'number' for"
                    + " numeric input.")
    public String askUser(String question, String uiType, List<String> options, List<Map<String,Object>> fiedls,Object defaultValue,Boolean allowOther) {

        String reason = question != null ? question : "Waiting for user input";

        throw new ToolSuspendException(reason);

    }


}
