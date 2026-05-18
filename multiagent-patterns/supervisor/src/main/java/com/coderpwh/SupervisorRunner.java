package com.coderpwh;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
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


    public SupervisorRunner(@Qualifier("supervisorAgent") ReActAgent supervisorAgent) {
        this.supervisorAgent = supervisorAgent;
    }


    /***
     * 运行
     * @param args
     * @throws Exception
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        String query1 = "Schedule a team standup for tomorrow at 9am";
        log.info("用户问题1:{}", query1);
        log.info("---");
        Msg response1 = supervisorAgent.call(buildUserMsg(query1)).block();
        log.info("AI助手回答: {}", getText(response1));
        log.info("-------------------------------------------------------------------------");

        String query2 =
                "Schedule a meeting with the design team next Tuesday at 2pm for 1 hour, "
                        + "and send them an email reminder about reviewing the new mockups.";
        log.info("用户问题1{}", query2);
        log.info("---");
        Msg response2 = supervisorAgent.call(buildUserMsg(query2)).block();
        log.info("AI助手回答: {}", getText(response2));

    }


    /***
     * 构建用户消息
     * @param text
     * @return
     */
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
