package com.coderpwh.advanced.hits;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.formatter.dashscope.DashScopeChatFormatter;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.session.InMemorySession;
import io.agentscope.core.session.Session;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.agentscope.core.tool.Toolkit;
import java.awt.*;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author coderpwh
 */
@SpringBootApplication
@RestController
@RequestMapping("/api")
public class HitlInteractionExample {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();


    private static final Set<String> TOOLS_REQUIRING_CONFIRMATION =
            Set.of(AddCalendarEventTool.TOOL_NAME);

    private static final String SYS_PROMPT =
            """
                    你是一位专业的健身教练助手，负责制定个性化的训练计划。
                    规则：
                    - 绝不在普通文本中提问。必须始终使用 ask_user 工具。
                    - 每次响应只能调用一次 ask_user。
                    - 使用与用户输入相同的语言进行回复。
                    需要收集的信息（每次收集一项，跳过用户已提供的内容）：
                    - 健身目标：单选 — 减脂、增肌、综合健身、柔韧性训练
                    - 身体信息（年龄、身高、体重）：包含数字输入框的表单
                    - 可用器材：多选（允许自定义）— 例如哑铃、杠铃、跑步机、引体向上架、弹力带（根据用户目标进行调整）
                    - 每周训练天数：数字
                    - 受伤 / 健康问题：先确认是否有，若有则填写文本
                    - 计划开始日期：日期
                    工作流程：
                    1. 通过 ask_user 逐一收集缺失信息。
                    2. 生成详细的每周计划，包括训练动作、组数、次数和休息时间。
                    3. 每个训练日调用一次 add_calendar_event，将其添加到日历中。
                    """;


    private final Session session = new InMemorySession();

    private final ConcurrentHashMap<String, ReActAgent> runningAgents = new ConcurrentHashMap<>();

    private final Toolkit toolkit;

    private final DashScopeChatModel model;


    {
        String apiKey = System.getenv("DASHSCOPE_API_KEY");
        toolkit = new Toolkit();
        toolkit.registerTool(new UserInteractionTool());
        toolkit.registerTool(new AddCalendarEventTool());

        model =
                DashScopeChatModel.builder().apiKey(apiKey).modelName("qwen-max").stream(true)
                        .enableThinking(false)
                        .formatter(new DashScopeChatFormatter())
                        .build();
    }

    public static void main(String[] args) {
        String apiKey = System.getenv("DASHSCOPE_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("Error: DASHSCOPE_API_KEY environment variable not set.");
            System.err.println("Please set it with: export DASHSCOPE_API_KEY=your_api_key");
            System.exit(1);
        }

        System.out.println("\n" + "=".repeat(70));
        System.out.println("  HITL Interactive UI Example");
        System.out.println("  Agent with dynamic UI-based user interaction");
        System.out.println("=".repeat(70));
        System.out.println("  Open: http://localhost:8080/hitl-interaction/index.html");
        System.out.println("=".repeat(70) + "\n");

        SpringApplication.run(HitlInteractionExample.class, args);
    }




}
