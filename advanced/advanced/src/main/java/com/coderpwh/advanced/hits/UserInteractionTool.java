package com.coderpwh.advanced.hits;

import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
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
    public String askUser(@ToolParam(name = "question", description = "The question to ask the user") String question,
                          @ToolParam(name = "ui_type", description = "UI component type: text, select, multi_select, confirm, form,date, number. Defaults to 'text'.") String uiType,
                          @ToolParam(name = "options", description = "Options for select/multi_select. Simple string array,e.g. [\\\"Beijing\\\", \\\"Shanghai\\\", \\\"Tokyo\\\"]") List<String> options,
                          @ToolParam(
                                  name = "fields",
                                  description =
                                          "Field definitions for 'form' ui_type. Array of objects with"
                                                  + " name, label, type (text/number/date/select/textarea),"
                                                  + " placeholder, required, options, min, max, step.",
                                  required = false) List<Map<String, Object>> fields,
                          @ToolParam(
                                  name = "default_value",
                                  description = "Default value for the input field (string only)",
                                  required = false) Object defaultValue,
                          @ToolParam(
                                  name = "allow_other",
                                  description =
                                          "If true, adds an 'Other' option with a free-text input so"
                                                  + " users can enter custom values not in the predefined"
                                                  + " list. Use with select or multi_select.",
                                  required = false) Boolean allowOther) {

        String reason = question != null ? question : "Waiting for user input";

        throw new ToolSuspendException(reason);

    }


}
