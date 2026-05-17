//package com.coderpwh.routing.graph;
//
//
//import com.coderpwh.routing.graph.service.RoutingGraphService;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.boot.ApplicationArguments;
//import org.springframework.boot.ApplicationRunner;
//import org.springframework.stereotype.Component;
//
//@Component
//public class RoutingGraphRunner implements ApplicationRunner {
//
//    private static final Logger log = LoggerFactory.getLogger(RoutingGraphRunner.class);
//
//    private final RoutingGraphService routingGraphService;
//
//    public RoutingGraphRunner(RoutingGraphService routingGraphService) {
//        this.routingGraphService = routingGraphService;
//    }
//
//
//    @Override
//    public void run(ApplicationArguments args) throws Exception {
//        String query = "搜索github上面的x001编码";
//        log.info("Query:{}", query);
//
//        RoutingGraphService.RoutingGraphResult result = routingGraphService.run(query);
//        log.info("最终答案:\n{}", result.finalAnswer());
//
//    }
//
//}
