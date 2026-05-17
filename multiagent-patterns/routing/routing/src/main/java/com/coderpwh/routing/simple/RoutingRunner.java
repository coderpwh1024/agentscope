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

        String query = "How do I authenticate API requests?";

        log.info("问题:{}",query);
        RouterService.RouteResult result = routerService.run(query);
        log.info("Classifications:");
        result.classifications().forEach(c -> log.info("  {}: {}", c.source(), c.query()));
        log.info("---");
        log.info("最终答案:{}",result.finalAnswer());
    }
}
