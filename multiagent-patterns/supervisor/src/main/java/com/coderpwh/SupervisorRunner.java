package com.coderpwh;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;


/**
 * SupervisorRunner
 *
 * @author coderpwh
 */
@Component
@Order(1)
@ConditionalOnProperty(name = "supervisor.run-examples", havingValue = "true")
public class SupervisorRunner implements ApplicationRunner {


    private final static Logger log = LoggerFactory.getLogger(SupervisorRunner.class);


    private final ReActAgent supervisorAgent;


    public SupervisorRunner(ReActAgent supervisorAgent) {
        this.supervisorAgent = supervisorAgent;
    }


    @Override
    public void run(ApplicationArguments args) throws Exception {

    }

    private static Msg buildUserMsg(String text) {
        return Msg.builder().role(MsgRole.USER).textContent(text).build();
    }


    /***
     *  获取响应文本
     * @param msg
     * @return
     */
    private static String getText(Msg msg) {
        return msg != null && StringUtils.hasText(msg.getTextContent())
                ? msg.getTextContent()
                : "(No response)";
    }

}
