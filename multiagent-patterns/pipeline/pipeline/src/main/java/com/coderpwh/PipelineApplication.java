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
public class PipelineApplication {


    public static void main(String[] args) {
        SpringApplication.run(PipelineApplication.class, args);
    }


    /***
     * 启动成功
     *
     * @param environment
     * @return
     */
    @Bean
    public ApplicationListener<ApplicationReadyEvent> applicationReadyEventListener(
            Environment environment) {
        return event -> {
            System.out.println("\n🎉========================================🎉");
            System.out.println("✅ Pipeline (Sequential / Parallel / Loop) example has started!");
            System.out.println(
                    "   Agents: sequential_sql_agent, parallel_research_agent,"
                            + " loop_sql_refinement_agent");
            System.out.println("🎉========================================🎉\n");
        };
    }


}
