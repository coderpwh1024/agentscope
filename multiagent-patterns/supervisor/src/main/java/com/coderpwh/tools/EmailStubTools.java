package com.coderpwh.tools;

import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;

import java.util.List;

/**
 * 邮箱工具类
 *
 * @author coderpwh
 */
public class EmailStubTools {


    /**
     * 发送邮件
     * @param to
     * @param subject
     * @param body
     * @param cc
     * @return
     */
    @Tool(name = "send_email", description = "Send an email via email API. Requires properly formatted addresses.")
    public String sendEmail(
            @ToolParam(name = "to", description = "List of recipient email addresses")
            List<String> to,
            @ToolParam(name = "subject", description = "Email subject") String subject,
            @ToolParam(name = "body", description = "Email body") String body,
            @ToolParam(name = "cc", description = "CC recipients", required = false)
            List<String> cc) {

        return String.format(
                "Email sent to %s - Subject: %s",
                String.join(", ", to != null ? to : List.of()), subject);
    }

}
