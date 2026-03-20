package com.coderpwh.agentscope.example;

import java.util.HashMap;
import java.util.Map;

import io.agentscope.core.plan.PlanNotebook;
import io.agentscope.core.plan.model.Plan;
import io.agentscope.core.plan.model.SubTask;
import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
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

        } catch (Exception e) {

        }

        return null;
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



}
