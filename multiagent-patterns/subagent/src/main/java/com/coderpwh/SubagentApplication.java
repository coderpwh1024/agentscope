package com.coderpwh;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/**
 * @author coderpwh
 */
@SpringBootApplication
public class SubagentApplication {

    public static void main(String[] args) {
        SpringApplication.run(SubagentApplication.class, args);
    }


    @Bean
    public ApplicationListener<ApplicationReadyEvent> applicationReadyEventListener(
            Environment environment) {
        return event -> {
            System.out.println("\n🎉========================================🎉");
            System.out.println("subagent 子智能体开始了");
            System.out.println("🎉========================================🎉\n");
        };
    }

}
