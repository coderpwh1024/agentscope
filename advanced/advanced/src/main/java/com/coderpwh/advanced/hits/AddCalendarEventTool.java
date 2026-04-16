package com.coderpwh.advanced.hits;

import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;

/**
 * @author coderpwh
 */
public class AddCalendarEventTool {

    public static final String TOOL_NAME = "add_calendar_event";

    @Tool(
            name = TOOL_NAME,
            description =
                    "Add a workout event to the user's calendar. Call this tool for EACH day's"
                            + " workout separately. For example, if the plan has workouts on Monday,"
                            + " Tuesday, and Wednesday, call this tool 3 times with each day's"
                            + " details.")
    public String addCalendarEvent(@ToolParam(name = "title", description = "Event title,e.g 'Chest + Triceps Workout' ") String title,
                                   @ToolParam(
                                           name = "date",
                                           description = "Event date in YYYY-MM-DD format, e.g. '2026-03-02'")
                                   String date, @ToolParam(
            name = "time",
            description =
                    "Start time in HH:mm format, e.g. '08:00'. Defaults to"
                            + " '09:00'.",
            required = false)
                                       String time, @ToolParam(
            name = "duration_minutes",
            description = "Duration in minutes, e.g. 60",
            required = false)
                                       Integer durationMinutes, @ToolParam(
            name = "description",
            description = "Detailed workout content for this session",
            required = false)
                                       String description) {

        String startTime = (time != null && !time.isEmpty()) ? time : "09:00";
        int duration = (durationMinutes != null && durationMinutes > 0) ? durationMinutes : 60;

        return String.format(
                "Successfully added calendar event: '%s' on %s at %s (%d min)",
                title, date, startTime, duration);
    }

}
