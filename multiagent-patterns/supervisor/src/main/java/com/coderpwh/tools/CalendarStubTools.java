package com.coderpwh.tools;

import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;

import java.util.List;

/**
 * @author coderpwh
 */
public class CalendarStubTools {


    @Tool(name = "create_calendar_event", description = "Create a calendar event. Requires exact ISO datetime format.")
    public String createCalendarEvent(
            @ToolParam(name = "title", description = "Event title") String title,
            @ToolParam(
                    name = "startTime",
                    description = "Start time in ISO format, e.g. 2024-01-15T14:00:00")
            String startTime,
            @ToolParam(
                    name = "endTime",
                    description = "End time in ISO format, e.g. 2024-01-15T15:00:00")
            String endTime,
            @ToolParam(name = "attendees", description = "List of attendee email addresses")
            List<String> attendees,
            @ToolParam(name = "location", description = "Event location", required = false)
            String location) {
        return String.format(
                "Event created: %s from %s to %s with %d attendees",
                title, startTime, endTime, attendees != null ? attendees.size() : 0);
    }

}
