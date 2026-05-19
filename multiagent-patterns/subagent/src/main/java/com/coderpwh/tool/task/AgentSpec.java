package com.coderpwh.tool.task;

import java.util.List;


/***
 * 代理规格
 * @param name
 * @param description
 * @param systemPrompt
 * @param toolNames
 * @param model
 */
public record AgentSpec(String name, String description, String systemPrompt, List<String> toolNames,   String model) {


    /**
     * 构造器
     * @param name
     * @param description
     * @param systemPrompt
     * @return
     */
    public static AgentSpec of(String name, String description, String systemPrompt) {
        return new AgentSpec(name, description, systemPrompt, List.of(), null);
    }


}
