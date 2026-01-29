package com.coderpwh.agentscope.example;

import java.awt.*;
import java.io.IOException;
import java.util.Map;

import io.agentscope.core.skill.AgentSkill;
import io.agentscope.core.skill.SkillBox;
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


    }

    private static void setupDataAnalysisSkills(Toolkit toolkit, SkillBox skillBox) {
        System.out.println("===========开始设置数据分析技能======\n");


        AgentSkill dataSkill = createDataAnalysisSkill();


    }


    /***
     * 创建数据分析技能
     * @return
     */
    private static AgentSkill createDataAnalysisSkill() {
        String skillMd =
                """
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

        Map<String, String> resources =
                Map.of(
                        "references/statistics-guide.md",
                        """
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
                                """,
                        "references/visualization-guide.md",
                        """
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
                                """,
                        "examples/sample-analysis.md",
                        """
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


}
