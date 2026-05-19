package com.coderpwh.tool.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.pattern.PathPattern;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.util.List;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

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





    /***
     * 获取代理
     * @param map
     * @param key
     * @return
     */

    private static String getString(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v != null ? v.toString().trim() : null;
    }

    /***
     *  从目录中加载代理
     * @param toolsStr
     * @return
     */
    private static List<String> parseToolNames(String toolsStr) {
        if (!StringUtils.hasText(toolsStr)) {
            return List.of();
        }
        return Stream.of(toolsStr.split(",")).map(String::trim).filter(StringUtils::hasText).toList();
    }


}
