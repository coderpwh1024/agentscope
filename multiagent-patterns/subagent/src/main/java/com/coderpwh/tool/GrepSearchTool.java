package com.coderpwh.tool;

import java.nio.file.Path;

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

    public String grepSearch(){

        return "grepSearch";
    }




}
