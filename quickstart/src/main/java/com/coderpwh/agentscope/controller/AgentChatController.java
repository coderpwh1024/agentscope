package com.coderpwh.agentscope.controller;


import com.coderpwh.agentscope.util.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author coderpwh
 */
@Slf4j
@RestController
@RequestMapping("/chat")
public class AgentChatController {


    /**
     * 健康检查
     */
    @GetMapping("/health")
    public Result<String> health() {
        log.info("Agent 服务运行正常");
        return Result.success("Agent 服务运行正常");
    }
}
