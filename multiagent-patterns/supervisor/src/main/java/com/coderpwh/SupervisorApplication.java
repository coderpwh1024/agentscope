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
public class SupervisorApplication {


    public static void main(String[] args) {
        SpringApplication.run(SupervisorApplication.class, args);
    }

    @Bean
    public ApplicationListener<ApplicationReadyEvent> applicationReadyEventListener(
            Environment environment) {
        return event -> {
            System.out.println("\n🎉========================================🎉");
            System.out.println("✅ Supervisor (personal assistant) example has started!");
            System.out.println("🎉========================================🎉\n");
        };
    }



}
