package com.coderpwh.tool.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.util.List;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 获取代理
 *
 * @author coderpwh
 */
public class AgentSpecLoader {

    private static final Logger logger = LoggerFactory.getLogger(AgentSpecLoader.class);


    private static final Yaml YAML = new Yaml();

    private AgentSpecLoader() {
    }




/*    public static List<AgentSpec> loadFromDirectory(String directoryPath) throws IOException {
        if (!StringUtils.hasText(directoryPath)) {
            return List.of();
        }
        return loadFromDirectory(Paths.get(directoryPath));
    }*/




}
