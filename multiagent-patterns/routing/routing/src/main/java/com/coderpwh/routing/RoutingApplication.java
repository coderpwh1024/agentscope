package com.coderpwh.routing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class RoutingApplication {

    public static void main(String[] args) {
        SpringApplication.run(RoutingApplication.class, args);
    }


    public ApplicationListener<ApplicationReadyEvent> applicationReadyEventApplicationListener(Environment enviroment) {

        return event->{
            System.out.println("===============================================================");
            System.out.println("启动成功");
            System.out.println("路由图示例已经启动了");
            System.out.println("===========================================================================");
        };
    }

}
