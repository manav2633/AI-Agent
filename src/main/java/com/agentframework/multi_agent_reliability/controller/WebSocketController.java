package com.agentframework.multi_agent_reliability.controller;

import com.agentframework.multi_agent_reliability.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Controller
public class WebSocketController {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketController.class);

    private final WebSocketNotificationService notificationService;
    private final AgentOrchestrationService orchestrationService;
    private final BenchmarkService benchmarkService;
    private final MetricsCollectionService metricsService;

    public WebSocketController(WebSocketNotificationService notificationService,
                               AgentOrchestrationService orchestrationService,
                               BenchmarkService benchmarkService,
                               MetricsCollectionService metricsService) {
        this.notificationService = notificationService;
        this.orchestrationService = orchestrationService;
        this.benchmarkService = benchmarkService;
        this.metricsService = metricsService;
    }

    @SubscribeMapping("/topic/system")
    public Map<String,Object> subscribeSystem(SimpMessageHeaderAccessor headers) {
        String sessionId = headers.getSessionId();
        logger.info("Client {} subscribed to /topic/system", sessionId);
        return Map.of(
                "type","WELCOME",
                "message","Connected to Multi-Agent Framework",
                "timestamp", LocalDateTime.now()
        );
    }

    @SubscribeMapping("/topic/executions")
    public Map<String,Object> subscribeExecutions() {
        return Map.of(
                "type","EXECUTION_SUBSCRIPTION",
                "recentExecutions", orchestrationService.getRecentExecutions(),
                "statistics", orchestrationService.getExecutionStatistics()
        );
    }

    @SubscribeMapping("/topic/benchmarks")
    public Map<String,Object> subscribeBenchmarks() {
        return Map.of(
                "type","BENCHMARK_SUBSCRIPTION",
                "activeBenchmarks", benchmarkService.getActiveBenchmarkRuns()
        );
    }

    @SubscribeMapping("/topic/metrics")
    public Map<String,Object> subscribeMetrics() {
        return Map.of(
                "type","METRICS_SUBSCRIPTION",
                "systemSummary", metricsService.getSystemPerformanceSummary(),
                "comparison", metricsService.getFrameworkComparison()
        );
    }

    @MessageMapping("/system/status")
    @SendTo("/topic/system")
    public Map<String,Object> systemStatus(@Payload Map<String,Object> req,
                                           SimpMessageHeaderAccessor headers) {
        logger.info("System status requested");
        Map<String,Object> status = new HashMap<>();
        status.put("type","SYSTEM_STATUS");
        status.put("timestamp", LocalDateTime.now());
        status.put("activeExecutions", orchestrationService.getRecentExecutions().size());
        status.put("activeBenchmarks", benchmarkService.getActiveBenchmarkRuns().size());
        return status;
    }

    @MessageMapping("/refresh")
    @SendTo("/topic/system")
    public Map<String,Object> handleRefresh(@Payload Map<String,Object> req) {
        String t = (String) req.get("type");
        Map<String,Object> resp = new HashMap<>();
        resp.put("type","REFRESH_RESPONSE");
        resp.put("timestamp", LocalDateTime.now());
        switch (t) {
            case "executions" -> resp.put("executions", orchestrationService.getRecentExecutions());
            case "benchmarks" -> resp.put("benchmarks", benchmarkService.getActiveBenchmarkRuns());
            case "metrics" -> resp.put("systemSummary", metricsService.getSystemPerformanceSummary());
            default -> resp.put("error","Unknown refresh type");
        }
        return resp;
    }

    @MessageMapping("/ping")
    @SendTo("/topic/system")
    public Map<String,Object> pong(@Payload Map<String,Object> req) {
        return Map.of(
                "type","PONG",
                "timestamp", LocalDateTime.now(),
                "clientTimestamp", req.get("timestamp")
        );
    }
}
