package com.coderpwh;

import org.springframework.context.annotation.Configuration;

/**
 * SupervisorConfig
 *
 * @author coderpwh
 */
@Configuration
public class SupervisorConfig {

    private static final String CALENDAR_AGENT_PROMPT =
            """
            你是一个日历调度助手。\
            将自然语言形式的调度请求（例如"下周二下午2点"）\
            解析为标准的 ISO 日期时间格式。\
            在需要时使用 get_available_time_slots 检查可用时间。\
            使用 create_calendar_event 来安排日程。\
            始终在最终回复中确认已安排的内容。
            """;

    private static final String EMAIL_AGENT_PROMPT =
            """
            你是一个邮件助手。\
            根据自然语言请求撰写专业的电子邮件。\
            提取收件人信息，并拟写恰当的主题和正文内容。\
            使用 send_email 来发送邮件。\
            始终在最终回复中确认已发送的内容。
            """;

    private static final String SUPERVISOR_PROMPT =
            """
            你是一个得力的个人助理。\
            你可以安排日历事件和发送电子邮件。\
            将用户请求拆解为相应的工具调用，并协调各项结果。\
            当一个请求涉及多个操作时，按顺序使用多个工具完成任务。
            """;

}
