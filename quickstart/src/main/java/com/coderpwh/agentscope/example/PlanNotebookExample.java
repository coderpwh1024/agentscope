package com.coderpwh.agentscope.example;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.coderpwh.agentscope.util.MsgUtils;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.formatter.dashscope.DashScopeChatFormatter;
import io.agentscope.core.hook.Hook;
import io.agentscope.core.hook.HookEvent;
import io.agentscope.core.hook.PostActingEvent;
import io.agentscope.core.memory.InMemoryMemory;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.plan.PlanNotebook;
import io.agentscope.core.plan.model.Plan;
import io.agentscope.core.plan.model.SubTask;
import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import io.agentscope.core.tool.Toolkit;
import reactor.core.publisher.Mono;

/**
 * @author coderpwh
 */
public class PlanNotebookExample {


    private static final Map<String, String> fileStorage = new HashMap<>();


    @Tool(name = "write_file", description = "Write content to a file")
    public Mono<String> writeFile(@ToolParam(name = "filename", description = "File name") String filename,
                                  @ToolParam(name = "content", description = "Content") String content) {
        System.out.println("\n📝 [write_file] " + filename + " (" + content.length() + " chars)");
        fileStorage.put(filename, content);
        return Mono.just("File saved:" + filename);

    }


    @Tool(name = "read_file", description = "Read content from a file")
    public Mono<String> readFile(@ToolParam(name = "filename", description = "File name") String filename) {
        System.out.println("read file" + filename);
        if (!fileStorage.containsKey(filename)) {
            return Mono.just("Error: File not found");
        }
        return Mono.just(fileStorage.get(filename));
    }


    @Tool(name = "calculate", description = "Basic math:+,-,*,/")
    public Mono<String> calculate(@ToolParam(name = "expression", description = "Math expression") String expression) {
        System.out.println("\n 计算表达式:" + expression);
        try {
            double result = evaluateExpression(expression);
            return Mono.just(expression + " = " + result);
        } catch (Exception e) {
            return Mono.just("Error: Invalid expression");
        }
    }

    private static double evaluateExpression(String expr) {
        expr = expr.replaceAll("\\s+", "");

        while (expr.contains("*") || expr.contains("/")) {
            String[] parts = expr.split("(?=[*/])|(?<=[*/])");
            for (int i = 0; i < parts.length; i++) {
                if (parts[i].equals("*") && i > 0 && i < parts.length - 1) {
                    double result = Double.parseDouble(parts[i - 1]) * Double.parseDouble(parts[i + 1]);

                    expr = expr.replaceFirst(parts[i - 1] + "\\*" + parts[i + 1], String.valueOf(result));

                } else if (parts[i].equals("/") && i > 0 && i < parts.length - 1) {
                    double result =
                            Double.parseDouble(parts[i - 1]) / Double.parseDouble(parts[i + 1]);

                    expr = expr.replaceFirst(parts[i - 1] + "/" + parts[i + 1], String.valueOf(result));
                    break;
                }

            }
        }
        String[] terms = expr.split("(?=[+\\-])|(?<=[+\\-])");
        double result = 0;
        String operator = "+";

        for (String term : terms) {
            if (term.equals("+") || term.equals("-")) {
                operator = term;
            } else if (!term.isEmpty()) {
                double value = Double.parseDouble(term);
                result = operator.equals("+") ? result + value : result - value;
            }
        }
        return result;
    }

    private static void printPlanState(PlanNotebook notebook, String event) {

        Plan currentPlan = notebook.getCurrentPlan();
        if (currentPlan == null) {
            System.out.println("evnent 无执行计划");
            return;
        }

        System.out.println("\n" + "=".repeat(70));
        System.out.println("📋 PLAN STATE [" + event + "]");
        System.out.println("=".repeat(70));
        System.out.println("Plan: " + currentPlan.getName());
        System.out.println("State: " + currentPlan.getState());
        System.out.println("\nSubtasks:");

        for (int i = 0; i < currentPlan.getSubtasks().size(); i++) {
            SubTask subtask = currentPlan.getSubtasks().get(i);
            String icon =
                    switch (subtask.getState()) {
                        case TODO -> "⏸️";
                        case IN_PROGRESS -> "▶️";
                        case DONE -> "✅";
                        case ABANDONED -> "❌";
                    };

            System.out.printf(
                    "  %s [%d] %s - %s%n", icon, i, subtask.getName(), subtask.getState());
        }
        System.out.println("=".repeat(70) + "\n");
    }

    public static void main(String[] args) throws IOException {
        ExampleUtils.printWelcome(
                "PlanNotebook 示例",
                "观察 Agent 如何逐步创建并执行计划！");

        String apiKey = ExampleUtils.getDashScopeApiKey();

        Toolkit toolkit = new Toolkit();
        toolkit.registerTool(new PlanNotebookExample());


        PlanNotebook planNotebook = PlanNotebook.builder().build();

        Hook planVisualizationHook = new Hook() {
            @Override
            public <T extends HookEvent> Mono<T> onEvent(T event) {
                if (event instanceof PostActingEvent postActingEvent) {
                    String toolName = postActingEvent.getToolUse().getName();
                    printPlanState(planNotebook, "After " + toolName);
                }
                return Mono.just(event);
            }
        };


        ReActAgent agent = ReActAgent
                .builder()
                .name("PlanAgent")
                .sysPrompt("""
                        你是一个严谨有序的助手。对于多步骤任务：\\n"
                        + "1. 使用 create_plan 工具创建计划\\n"
                        + "2. 逐一执行子任务\\n"
                        + "3. 每完成一个子任务后调用 finish_subtask\\n"
                        + "4. 所有任务完成后调用 finish_plan
                        """)
                .model(
                        DashScopeChatModel
                                .builder()
                                .apiKey(apiKey)
                                .modelName("qwen-plus")
                                .stream(false)
                                .formatter(new DashScopeChatFormatter())
                                .build()
                )
                .memory(new InMemoryMemory())
                .toolkit(toolkit)
                .maxIters(100)
                .hooks(List.of(planVisualizationHook))
                .planNotebook(planNotebook)
                .build();

        System.out.println("\n" + "=".repeat(70));
        System.out.println("任务");
        System.out.println("=".repeat(70));
        String userInput =
                "计算一个长方形的面积（长=10，宽=5），然后将结果保存到"
                        + " 'result.txt' 文件中，并通过读取文件来验证结果。这是一个多步骤任务——"
                        + " 请制定计划后有序执行。";
        System.out.println(userInput);
        System.out.println("=".repeat(70) + "\n");

        Msg userMsg = Msg
                .builder()
                .role(MsgRole.USER)
                .content(TextBlock.builder().text(userInput).build())
                .build();

        System.out.println("开始执行任务...");

        Msg response = agent.call(userMsg).block();

        System.out.println("\n" + "=".repeat(70));
        System.out.println("FINAL RESPONSE");
        System.out.println("=".repeat(70));
        String finalText = MsgUtils.getTextContent(response);
        System.out.println(finalText != null ? finalText : "(No response)");
        System.out.println("=".repeat(70) + "\n");

        if (fileStorage.containsKey("result.txt")) {
            System.out.println("保存文件内容:");
            System.out.println("  " + fileStorage.get("result.txt"));
        }


    }


}
