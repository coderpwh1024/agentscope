//package com.coderpwh.routing.graph.service;
//
//import com.alibaba.cloud.ai.graph.CompiledGraph;
//import com.alibaba.cloud.ai.graph.OverAllState;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.util.Map;
//import java.util.Optional;
//
//public class RoutingGraphService {
//
//    private static final Logger log = LoggerFactory.getLogger(RoutingGraphService.class);
//
//    private final CompiledGraph routingGraph;
//
//
//    public RoutingGraphService(CompiledGraph routingGraph) {
//        this.routingGraph = routingGraph;
//    }
//
//
//    public RoutingGraphResult run(String query) {
//        Map<String, Object> inputs = Map.of("input", query);
//        Optional<OverAllState> resultOpt = routingGraph.invoke(inputs);
//
//        if (resultOpt.isEmpty()) {
//            return new RoutingGraphResult(query, null, "No result from graph");
//        }
//
//        OverAllState state = resultOpt.get();
//        String finalAnswer = state.value("final_answer")
//                .map(Object::toString)
//                .orElse(state.value("merged_result").map(Object::toString).orElse("No result."));
//
//        log.info("路由图已完成 answer length={}", finalAnswer.length());
//
//        return new RoutingGraphResult(query, state, finalAnswer);
//    }
//
//    public record RoutingGraphResult(String query, OverAllState state, String finalAnswer) {
//    }
//
//
//}
