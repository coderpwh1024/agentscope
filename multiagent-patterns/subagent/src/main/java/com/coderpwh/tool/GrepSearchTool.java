package com.coderpwh.tool;

import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * 搜索工具
 *
 * @author coderpwh
 */
public class GrepSearchTool {

    private final Path workspacePath;

    public GrepSearchTool(Path workspacePath) {
        this.workspacePath = workspacePath.toAbsolutePath().normalize();
    }


    /***
     * 搜索
     * @param pattern
     * @param filePattern
     * @return
     */
    @Tool(
            name = "grep_search",
            description =
                    "Search file contents for a regex pattern. Use to find code, dependencies, or"
                            + " text in the workspace.")
    public String grepSearch(@ToolParam(name = "pattern", description = "Regex pattern to search for")
                             String pattern,
                             @ToolParam(
                                     name = "filePattern",
                                     description = "Optional glob to limit files (e.g. **/*.java)",
                                     required = false)
                             String filePattern) {

        if (pattern == null || pattern.isBlank()) {
            return "Error: pattern is required.";
        }
        try {
            Pattern regex = Pattern.compile(pattern);
            Stream<Path> fileStream = Files.walk(workspacePath).filter(Files::isRegularFile);
            if (filePattern != null && !filePattern.isBlank()) {
                var matcher =
                        java.nio.file.FileSystems.getDefault()
                                .getPathMatcher("glob:" + filePattern);
                fileStream = fileStream.filter(p -> matcher.matches(workspacePath.relativize(p)));
            }
            StringBuilder out = new StringBuilder();
            List<Path> files;
            try (Stream<Path> walk = fileStream.limit(100)) {
                files = walk.toList();
            }
            for (Path file : files) {
                try {
                    Files.lines(file)
                            .forEach(
                                    line -> {
                                        if (regex.matcher(line).find()) {
                                            out.append(workspacePath.relativize(file))
                                                    .append(": ")
                                                    .append(line)
                                                    .append("\n");
                                        }
                                    });
                } catch (Exception ignored) {
                }
            }
            String result = out.toString();
            return result.isEmpty() ? "No matches for " + pattern : result;
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    public static GrepSearchTool create(Path workspacePath) {
        return new GrepSearchTool(workspacePath);
    }


}
