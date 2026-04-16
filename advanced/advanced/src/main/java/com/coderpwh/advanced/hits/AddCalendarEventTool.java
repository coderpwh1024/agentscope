package com.coderpwh.advanced.hits;

import io.agentscope.core.tool.Tool;

/**
 * @author coderpwh
 */
public class AddCalendarEventTool {


    public static  final String TOOL_NAME = "add_calendar_event";


    @Tool(
            name = TOOL_NAME,
            description =
                    "Add a workout event to the user's calendar. Call this tool for EACH day's"
                            + " workout separately. For example, if the plan has workouts on Monday,"
                            + " Tuesday, and Wednesday, call this tool 3 times with each day's"
                            + " details.")
    public String addCalendarEvent(){

        return  null;

    }

}
