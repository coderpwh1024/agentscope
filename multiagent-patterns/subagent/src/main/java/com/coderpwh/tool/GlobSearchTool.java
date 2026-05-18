package com.coderpwh.tool;

import io.agentscope.core.tool.Tool;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * glob搜索工具
 *
 * @author coderpwh
 */
public class GlobSearchTool {

    private final Path workspacePath;

    public GlobSearchTool(Path workspacePath) {
        this.workspacePath = workspacePath.toAbsolutePath().normalize();
    }


    /***
     * glob搜索
     * @param pattern
     * @return
     */
    @Tool(name = "glob_search", description = "Search for files matching a glob pattern (e.g. **/*.java, src/**/*.ts) under\"\n" +
            "                            + \" the workspace.")
    public String globSearch(String pattern) {

        if (pattern == null || pattern.isBlank()) {
            return "Error: pattern is required.";
        }

        try {
            PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
            try (Stream<Path> walk = Files.walk(workspacePath)) {
                String result =
                        walk.filter(Files::isRegularFile)
                                .filter(p -> matcher.matches(workspacePath.relativize(p)))
                                .limit(200)
                                .map(p -> workspacePath.relativize(p).toString())
                                .collect(Collectors.joining("\n"));
                return result.isEmpty() ? "No files matched " + pattern : result;
            }

        }catch (Exception e){
            return "错误信息:"+e.getMessage();
        }


    }


    /**
     * 创建
     *
     * @param workspacePath
     * @return
     */
    public static GlobSearchTool create(Path workspacePath) {
        return new GlobSearchTool(workspacePath);
    }

}
