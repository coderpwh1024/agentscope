package com.coderpwh.config;

import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.model.Model;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author coderpwh
 */
@Configuration
public class PipelineModelConfig {



    @Bean
    public Model dashScopeChatModel() {
//        String key = System.getenv("DASH_SCOPE_KEY");
        String key ="";
        return DashScopeChatModel.builder().apiKey(key).modelName("qwen-plus").build();
    }

}
