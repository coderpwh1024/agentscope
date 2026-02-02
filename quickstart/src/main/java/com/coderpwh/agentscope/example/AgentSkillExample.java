package com.coderpwh.agentscope.example;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.formatter.dashscope.DashScopeChatFormatter;
import io.agentscope.core.memory.InMemoryMemory;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.skill.AgentSkill;
import io.agentscope.core.skill.SkillBox;
import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.core.skill.util.SkillUtil;

/**
 * @author coderpwh
 */
public class AgentSkillExample {

    public static void main(String[] args) throws IOException {


        String apikey = ExampleUtils.getDashScopeApiKey();

        Toolkit toolkit = new Toolkit();
        SkillBox skillBox = new SkillBox(toolkit);
        setupDataAnalysisSkills(toolkit, skillBox);

        ReActAgent agent = ReActAgent.builder()
                .name("DataAnalyst")
                .sysPrompt(buildSystemPrompt())
                .model(
                        DashScopeChatModel.builder()
                                .apiKey(apikey)
                                .modelName("qwen-max")
                                .stream(true).enableThinking(true)
                                .formatter(new DashScopeChatFormatter())
                                .build()

                )
                .toolkit(toolkit)
                .skillBox(skillBox)
                .memory(new InMemoryMemory())
                .build();


        // 打印
        printExampleQueries();

        ExampleUtils.startChat(agent);


    }

    private static void setupDataAnalysisSkills(Toolkit toolkit, SkillBox skillBox) {
        System.out.println("===========开始设置数据分析技能======\n");


        AgentSkill dataSkill = createDataAnalysisSkill();
        // TOOL 工具
        skillBox.registration().tool(new DataAnalysisTools()).skill(dataSkill).apply();

        System.out.println("✓ 已注册技能: " + dataSkill.getName());
        System.out.println("  描述: " + dataSkill.getDescription());
        System.out.println("  资源: " + dataSkill.getResources().size() + " 个文件");
        System.out.println("\n✓ 已注册数据分析工具:");
        System.out.println("  - load_sales_data: 加载示例销售数据");
        System.out.println("  - calculate_statistics: 计算平均值、中位数、标准差");
        System.out.println("  - analyze_trend: 分析数据趋势");
        System.out.println("  - generate_chart: 生成可视化描述");
        System.out.println("  - create_report: 创建分析报告");
        System.out.println("\n✓ 技能加载工具将在构建代理时自动注册\n");


    }


    /***
     * 创建数据分析技能
     * @return
     */
    private static AgentSkill createDataAnalysisSkill() {
        String skillMd = """
                ---
                name: data_analysis
                description: Use this skill when you need to analyze sales data, calculate statistics, identify trends, or generate reports. This skill provides comprehensive data analysis capabilities.
                ---

                # Data Analysis Skill

                ## Overview
                This skill enables you to perform comprehensive data analysis on sales data, including:
                - Loading and inspecting data
                - Statistical analysis (mean, median, standard deviation)
                - Trend analysis and pattern detection
                - Data visualization
                - Report generation

                ## When to Use This Skill
                Use this skill when the user asks to:
                - Analyze sales data or performance
                - Calculate statistics or metrics
                - Identify trends or patterns
                - Generate charts or visualizations
                - Create analysis reports

                ## Available Tools
                1. **load_sales_data**: Load sample sales data for analysis
                   - Returns: Dataset with sales records (date, product, amount, quantity)

                2. **calculate_statistics**: Calculate statistical metrics
                   - Input: field name (e.g., "amount", "quantity")
                   - Returns: mean, median, standard deviation, min, max

                3. **analyze_trend**: Analyze trends in the data
                   - Input: field name to analyze
                   - Returns: trend direction (increasing/decreasing/stable) and insights

                4. **generate_chart**: Generate chart visualization description
                   - Input: chart type (bar, line, pie) and field name
                   - Returns: Chart description and key insights

                5. **create_report**: Create comprehensive analysis report
                   - Returns: Formatted report with all analysis results

                ## Workflow
                Follow this workflow for data analysis tasks:
                1. Load data using load_sales_data
                2. Calculate statistics for relevant fields
                3. Analyze trends if needed
                4. Generate visualizations if requested
                5. Create final report summarizing findings

                ## Best Practices
                - Always load data first before analysis
                - Calculate statistics for numeric fields only
                - Provide clear interpretations of results
                - Include visualizations when helpful
                - Summarize key findings in the report

                ## Resources
                For detailed information, refer to:
                - references/statistics-guide.md: Statistical formulas and interpretations
                - references/visualization-guide.md: Chart types and best practices
                - examples/sample-analysis.md: Example analysis workflow
                """;

        Map<String, String> resources = Map.of("references/statistics-guide.md", """
                # Statistics Guide

                ## Key Metrics

                ### Mean (Average)
                - Formula: sum(values) / count(values)
                - Use: Central tendency measure
                - Interpretation: Typical value in the dataset

                ### Median
                - Formula: Middle value when sorted
                - Use: Robust central tendency (not affected by outliers)
                - Interpretation: 50th percentile value

                ### Standard Deviation
                - Formula: sqrt(sum((x - mean)^2) / count)
                - Use: Measure of data spread
                - Interpretation: How much values vary from mean

                ## Trend Analysis
                - Increasing: Values generally going up over time
                - Decreasing: Values generally going down over time
                - Stable: Values remain relatively constant
                - Volatile: Large fluctuations in values
                """, "references/visualization-guide.md", """
                # Visualization Guide

                ## Chart Types

                ### Bar Chart
                - Best for: Comparing categories
                - Example: Sales by product, revenue by region

                ### Line Chart
                - Best for: Showing trends over time
                - Example: Monthly sales, daily revenue

                ### Pie Chart
                - Best for: Showing proportions/percentages
                - Example: Market share, category distribution

                ## Best Practices
                1. Choose appropriate chart type for data
                2. Label axes clearly
                3. Use colors meaningfully
                4. Include title and legend
                5. Highlight key insights
                """, "examples/sample-analysis.md", """
                # Sample Analysis Workflow

                ## Example: Monthly Sales Analysis

                ### Step 1: Load Data
                Use load_sales_data to get the dataset

                ### Step 2: Calculate Statistics
                - Calculate statistics for "amount" field
                - Calculate statistics for "quantity" field

                ### Step 3: Analyze Trends
                - Analyze trend for "amount" over time
                - Identify patterns and seasonality

                ### Step 4: Visualize
                - Generate line chart for sales trend
                - Generate bar chart for product comparison

                ### Step 5: Report
                - Create comprehensive report
                - Include key findings and recommendations
                """);

        return SkillUtil.createFrom(skillMd, resources);
    }


    /***
     * 构建系统提示词
     * @return
     */
    private static String buildSystemPrompt() {
        return """
                您是一位专业的数据分析助理,擅长销售数据分析。

                您可以使用数据分析技能和工具。当用户询问有关数据分析的问题时:
                1. 使用 data_analysis 技能来访问工具和指导
                2. 遵循技能说明中推荐的工作流程
                3. 提供清晰的分析说明
                4. 在业务背景下解读结果
                5. 提供可操作的见解和建议

                始终要全面深入地进行分析,并清楚地解释您的推理过程。
                """;

    }


    /***
     * 输出示例问题
     */
    private static void printExampleQueries() {
        System.out.println("\n=== 可以尝试的示例查询 ===\n");
        System.out.println("1. \"分析销售数据并给我关键统计信息\"");
        System.out.println("2. \"销售额的趋势是什么?\"");
        System.out.println("3. \"显示按产品分类的销售可视化图表\"");
        System.out.println("4. \"创建一份综合分析报告\"");
        System.out.println("5. \"比较不同产品的表现\"");
        System.out.println("\n==================================\n");
    }


    public static class DataAnalysisTools {

        private static final List<SalesRecord> SALES_DATA = Arrays.asList(new SalesRecord("2024-01", "Laptop", 1200.00, 5), new SalesRecord("2024-01", "Mouse", 25.00, 20), new SalesRecord("2024-01", "Keyboard", 75.00, 15), new SalesRecord("2024-02", "Laptop", 1200.00, 8), new SalesRecord("2024-02", "Mouse", 25.00, 25), new SalesRecord("2024-02", "Keyboard", 75.00, 18), new SalesRecord("2024-03", "Laptop", 1200.00, 12), new SalesRecord("2024-03", "Mouse", 25.00, 30), new SalesRecord("2024-03", "Keyboard", 75.00, 22), new SalesRecord("2024-04", "Laptop", 1200.00, 10), new SalesRecord("2024-04", "Mouse", 25.00, 28), new SalesRecord("2024-04", "Keyboard", 75.00, 20));


        @Tool(name = "load_sales_data", description = "Load sample sales data for analysis. Returns dataset with columns: date, product,amount,quantity")
        public String loadSalesData() {
            StringBuilder sb = new StringBuilder();
            sb.append("销售数据加载成功");
            sb.append("========================");
            sb.append(String.format("Total Records: %d\n", SALES_DATA.size()));
            sb.append("记录为:\n");
            sb.append("--------------------------------------------------------\n");

            for (int i = 0; i < Math.min(5, SALES_DATA.size()); i++) {
                SalesRecord record = SALES_DATA.get(i);
                sb.append(String.format("日期: %s, 产品: %s, 金额: %.2f, 数量: %d\n", record.date, record.product, record.amount, record.quantity));
            }
            sb.append("日期:");
            sb.append("日期范围:2024-01 to 2024-04");
            sb.append("产品:Laptop, Mouse, Keyboard\n");
            sb.append("字段: date,product,amount,quantity");
            return sb.toString();
        }

        @Tool(name = "calculate_statistics", description = "Calculate statistical metrics (mean, median, std dev, min, max) for a numeric field")
        public String calculateStatistics(@ToolParam(name = "field", description = "Field name to analyze: 'amount' or 'quantity'") String field) {

            if (!field.equals("amount") && !field.equals("quantity")) {
                return "错误:无效字段。请使用 'amount' 或 'quantity'";
            }

            List<Double> values = SALES_DATA.stream().map(r -> field.equals("amount") ? r.amount : (double) r.quantity).sorted().collect(Collectors.toList());

            double mean = values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

            double median = values.size() % 2 == 0 ? (values.get(values.size() / 2 - 1) + values.get(values.size() / 2)) / 2.0 : values.get(values.size() / 2);


            double variance = values.stream().mapToDouble(v -> Math.pow(v - mean, 2)).average().orElse(0.0);

            double stdDev = Math.sqrt(variance);

            double min = values.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
            double max = values.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("'%s' 的统计信息:\n", field));
            sb.append("================================\n");
            sb.append(String.format("平均值:              %.2f\n", mean));
            sb.append(String.format("中位数:              %.2f\n", median));
            sb.append(String.format("标准差:              %.2f\n", stdDev));
            sb.append(String.format("最小值:              %.2f\n", min));
            sb.append(String.format("最大值:              %.2f\n", max));
            sb.append(String.format("范围:                %.2f\n", max - min));
            sb.append(String.format("样本数量:            %d\n", values.size()));
            sb.append("\n解释说明:\n");

            double cv = (stdDev / mean) * 100;
            if (cv < 20) {
                sb.append("- 低变异性: 数据点接近平均值\n");
            } else if (cv < 50) {
                sb.append("- 中等变异性: 数据有一定分散度\n");
            } else {
                sb.append("- 高变异性: 数据点分布广泛\n");
            }

            return sb.toString();
        }

        @Tool(name = "analyze_trend", description = "Analyze trend in data over time for a specific field")
        public String analyzeTrend(@ToolParam(name = "field", description = "Field name to analyze: 'amount' or 'quantity'") String field) {
            if (!field.equals("amount") && !field.equals("quantity")) {
                return "错误: 无效字段。请使用 'amount' 或 'quantity'";
            }

            Map<String, Double> monthlyData = new LinkedHashMap<>();
            for (SalesRecord record : SALES_DATA) {
                monthlyData.merge(record.date, field.equals("amount") ? record.amount * record.quantity : record.quantity, Double::sum);
            }

            List<Double> values = new ArrayList<>(monthlyData.values());
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("'%s' 的时间趋势:\n", field));
            sb.append("================================\n");

            double firstValue = values.get(0);
            double lastValue = values.get(values.size() - 1);
            double change = lastValue - firstValue;
            double percentChange = (change / firstValue) * 100;

            sb.append("\nMonthly Data:\n");
            for (Map.Entry<String, Double> entry : monthlyData.entrySet()) {
                sb.append(String.format("%s: %.2f\n", entry.getKey(), entry.getValue()));
            }
            sb.append("\n趋势摘要：\n");
            sb.append(String.format("  首期数值: %.2f\n", firstValue));
            sb.append(String.format("  末期数值: %.2f\n", lastValue));
            sb.append(String.format("  变化量:   %.2f (%.1f%%)\n", change, percentChange));
            String trend;
            if (percentChange > 10) {
                trend = "INCREASING";
                sb.append("\n✓ 趋势：上升 - 检测到强劲上升趋势\n");
            } else if (percentChange < -10) {
                trend = "DECREASING";
                sb.append("\n✓ 趋势：下降 - 检测到下降趋势\n");
            } else {
                trend = "STABLE";
                sb.append("\n✓ 趋势：稳定 - 数值保持相对稳定\n");
            }

            sb.append("\n解释说明：\n");
            if (trend.equals("INCREASING")) {
                sb.append("- 增长势头良好\n");
                sb.append("- 建议增加库存\n");
                sb.append("- 存在扩张机会\n");
            } else if (trend.equals("DECREASING")) {
                sb.append("- 业绩下滑需要关注\n");
                sb.append("- 建议审查定价和营销策略\n");
                sb.append("- 需调查根本原因\n");
            } else {
                sb.append("- 表现稳定\n");
                sb.append("- 保持当前策略\n");
                sb.append("- 寻找优化机会\n");
            }
            return sb.toString();
        }


        @Tool(name = "generate_chart", description = "Generate chart visualization description for the data")
        public String generateChart(@ToolParam(name = "chart_type", description = "Chart type: 'bar', 'line', or 'pie'") String chartType, @ToolParam(name = "field", description = "Field to visualize: 'amount' or 'quantity'") String field) {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("%s Chart: %s Analysis\n", capitalize(chartType), capitalize(field)));
            sb.append("================================\n\n");

            if (chartType.equalsIgnoreCase("bar")) {
                Map<String, Double> productData = new LinkedHashMap<>();
                for (SalesRecord record : SALES_DATA) {
                    productData.merge(record.product, field.equals("amount") ? record.amount * record.quantity : record.quantity, Double::sum);
                }
                sb.append("Product performance Comparison:\n\n");
                double maxValue = productData.values().stream().mapToDouble(Double::doubleValue).max().orElse(1.0);

                for (Map.Entry<String, Double> entry : productData.entrySet()) {
                    int barLength = (int) ((entry.getValue() / maxValue) * 40);
                    sb.append(String.format("%-12s |%s %.2f\n", entry.getKey(), "█".repeat(barLength), entry.getValue()));
                }
                sb.append("\nKey Insights:\n");
                String topProduct = productData.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse("N/A");
                sb.append(String.format("- Top Performer: %s\n", topProduct));
                sb.append("- Clear performance differences between products\n");
            } else if (chartType.equalsIgnoreCase("line")) {
                Map<String, Double> monthlyData = new LinkedHashMap<>();
                for (SalesRecord record : SALES_DATA) {
                    monthlyData.merge(record.date, field.equals("amount") ? record.amount * record.quantity : record.quantity, Double::sum);
                }
                sb.append("Trend Over Time:\n\n");
                double maxValue = monthlyData.values().stream().mapToDouble(Double::doubleValue).max().orElse(1.0);

                for (Map.Entry<String, Double> entry : monthlyData.entrySet()) {
                    int lineHeight = (int) ((entry.getValue() / maxValue) * 10);
                    sb.append(String.format("%s: %s%.2f\n", entry.getKey(), " ".repeat(lineHeight) + "●", entry.getValue()));

                    sb.append("\n关键见解:\n");
                    sb.append("- 时间序列显示出清晰的模式\n");
                    sb.append("- 有助于识别季节性趋势\n");
                }

            } else if (chartType.equalsIgnoreCase("pie")) {
                Map<String, Double> productData = new LinkedHashMap<>();
                for (SalesRecord record : SALES_DATA) {
                    productData.merge(record.product, field.equals("amount") ? record.amount * record.quantity : record.quantity, Double::sum);
                }
                double total = productData.values().stream().mapToDouble(Double::doubleValue).sum();
                sb.append("Market Share Distribution:\n\n");
                for (Map.Entry<String, Double> entry : productData.entrySet()) {
                    double percentage = (entry.getValue() / total) * 100;
                    sb.append(String.format("%-12s: %.1f%% (%.2f)\n", entry.getKey(), percentage, entry.getValue()));
                }
                sb.append("\n关键见解:\n");
                sb.append("- 显示每个产品的相对贡献\n");
                sb.append("- 有助于识别市场集中度\n");
            }

            return sb.toString();
        }

        @Tool(name = "create_report", description = "Create comprehensive analysis report with all findings")
        public String createReport() {
            StringBuilder sb = new StringBuilder();
            sb.append("═══════════════════════════════════════════════\n");
            sb.append("        SALES DATA ANALYSIS REPORT\n");
            sb.append("═══════════════════════════════════════════════\n\n");

            sb.append(
                    "Report Generated: "
                            + java.time.LocalDateTime.now()
                            .format(
                                    java.time.format.DateTimeFormatter.ofPattern(
                                            "yyyy-MM-dd HH:mm:ss"))
                            + "\n\n");

            sb.append("1. 执行摘要\n");
            sb.append("───────────────────────────────────────────────\n");
            sb.append("本报告提供了销售数据的综合分析\n");
            sb.append("涵盖2024年第一季度(1月至4月)。\n\n");
            sb.append("2. 数据概览\n");
            sb.append("───────────────────────────────────────────────\n");
            sb.append("总记录数: " + SALES_DATA.size() + "\n");
            sb.append("时间周期: 2024-01 至 2024-04\n");
            sb.append("分析产品: 笔记本电脑、鼠标、键盘\n\n");
            sb.append("3. 关键指标\n");
            sb.append("───────────────────────────────────────────────\n");

            double totalRevenue = SALES_DATA.stream().mapToDouble(record -> record.amount * record.quantity).sum();
            int totalQuantity = SALES_DATA.stream().mapToInt(record -> record.quantity).sum();

            sb.append(String.format("总收入:          $%.2f\n", totalRevenue));
            sb.append(String.format("总销售数量:      %d\n", totalQuantity));
            sb.append(
                    String.format("平均订单金额:    $%.2f\n\n", totalRevenue / SALES_DATA.size()));
            sb.append("4. 产品表现\n");
            sb.append("───────────────────────────────────────────────\n");

            Map<String, ProductStats> productStats = new LinkedHashMap<>();
            for (SalesRecord record : SALES_DATA) {
                productStats
                        .computeIfAbsent(record.product, k -> new ProductStats())
                        .add(record.amount * record.quantity, record.quantity);
            }

            for (Map.Entry<String, ProductStats> entry : productStats.entrySet()) {
                ProductStats stats = entry.getValue();
                sb.append(String.format("%s: %d 个, $%.2f\n", entry.getKey(), stats.units, stats.revenue));
            }
            sb.append("\n5. 建议\n");
            sb.append("───────────────────────────────────────────────\n");
            sb.append("• 专注于高性能产品\n");
            sb.append("• 监控趋势以获取预警信号\n");
            sb.append("• 考虑季节性促销活动\n");
            sb.append("• 根据需求模式优化库存\n\n");
            sb.append("═══════════════════════════════════════════════\n");
            sb.append("              报告结束\n");
            sb.append("═══════════════════════════════════════════════\n");

            return sb.toString();
        }

        private static String capitalize(String str) {
            return str.substring(0, 1).toUpperCase() + str.substring(1).toUpperCase();
        }


    }

    static class SalesRecord {
        String date;

        String product;

        double amount;

        int quantity;

        SalesRecord(String date, String product, double amount, int quantity) {
            this.date = date;
            this.product = product;
            this.amount = amount;
            this.quantity = quantity;
        }

    }

    static class ProductStats {
        double revenue = 0;
        int units = 0;

        void add(double rev, int qty) {
            revenue += rev;
            units += qty;
        }
    }


}
