package com.coderpwh.htilchat.service;

import io.agentscope.core.message.ToolResultBlock;
import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * @author coderpwh
 */
public class BuiltinTools {


    private final Random random = new Random();


    /***
     * 获取当前的时间
     * @return
     */
    @Tool(name = "get_time", description = "获取当前的日期和时间")
    public ToolResultBlock getTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return ToolResultBlock.text("Current time: " + now.format(formatter));
    }


    @Tool(name = "random_number",description = "生成一个随机数")
    public ToolResultBlock randomNumber(@ToolParam(name = "min", description = "Minimum value (inclusive)")int min, @ToolParam(name = "max", description = "Maximum value (inclusive)") int max) {
        if (min > max) {
            return ToolResultBlock.error("min must be less than or equal to max");
        }
        int result = random.nextInt(max - min + 1) + min;
        return ToolResultBlock.text("Random number between " + min + " and " + max + ": " + result);
    }


}
