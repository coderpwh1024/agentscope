package com.coderpwh.routing.simple;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "routing.runner.enabled", havingValue = "true")
public class RoutingRunner implements ApplicationRunner {


    private static final Logger log = LoggerFactory.getLogger(RoutingRunner.class);

    private final RouterService routerService;

    public RoutingRunner(RouterService routerService) {
        this.routerService = routerService;
    }
    @Override
    public void run(ApplicationArguments args) throws Exception {

    }
}
