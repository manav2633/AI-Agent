package com.agentframework.multi_agent_reliability.controller;

import com.agentframework.multi_agent_reliability.dto.MetricsResponse;
import com.agentframework.multi_agent_reliability.model.AgentFrameworkType;
import com.agentframework.multi_agent_reliability.service.MetricsCollectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/metrics")
@CrossOrigin(origins = "*")
public class MetricsController {

    private static final Logger logger = LoggerFactory.getLogger(MetricsController.class);
    private final MetricsCollectionService metricsService;

    public MetricsController(MetricsCollectionService metricsService) {
        this.metricsService = metricsService;
    }

    @GetMapping("/comparison")
    public ResponseEntity<List<MetricsResponse>> getFrameworkComparison() {
        return ResponseEntity.ok(metricsService.getFrameworkComparison());
    }

    @GetMapping("/system/summary")
    public ResponseEntity<Map<String,Object>> getSystemPerformanceSummary() {
        return ResponseEntity.ok(metricsService.getSystemPerformanceSummary());
    }

    @GetMapping("/top-performers")
    public ResponseEntity<List<Map<String,Object>>> getTopPerformers() {
        return ResponseEntity.ok(metricsService.getTopPerformingFrameworks());
    }

    @GetMapping("/reliability/distribution")
    public ResponseEntity<Map<String,Object>> getReliabilityDistribution() {
        return ResponseEntity.ok(metricsService.getReliabilityDistribution());
    }

    @GetMapping("/trends/{frameworkType}")
    public ResponseEntity<List<Map<String,Object>>> getTrend(
            @PathVariable AgentFrameworkType frameworkType) {
        return ResponseEntity.ok(metricsService.getFrameworkPerformanceTrend(frameworkType));
    }

    @GetMapping("/statistics/{frameworkType}")
    public ResponseEntity<Map<String,Object>> getStatSummary(
            @PathVariable AgentFrameworkType frameworkType) {
        return ResponseEntity.ok(metricsService.getFrameworkStatisticalSummary(frameworkType));
    }

    @PostMapping("/calculate/{benchmarkRunId}/{frameworkType}")
    public ResponseEntity<Map<String,Object>> calculateFrameworkMetrics(
            @PathVariable String benchmarkRunId,
            @PathVariable AgentFrameworkType frameworkType) {
        metricsService.calculateFrameworkMetrics(benchmarkRunId, frameworkType);
        return ResponseEntity.accepted().body(Map.of(
                "benchmarkRunId", benchmarkRunId,
                "frameworkType", frameworkType,
                "status", "processing"
        ));
    }

    @PostMapping("/calculate/{benchmarkRunId}")
    public ResponseEntity<Map<String,Object>> calculateAllMetrics(
            @PathVariable String benchmarkRunId) {
        metricsService.calculateAllFrameworkMetrics(benchmarkRunId);
        return ResponseEntity.accepted().body(Map.of(
                "benchmarkRunId", benchmarkRunId,
                "status", "processing"
        ));
    }

    @GetMapping("/frameworks")
    public ResponseEntity<List<AgentFrameworkType>> getFrameworks() {
        return ResponseEntity.ok(List.of(AgentFrameworkType.values()));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String,Object>> getDashboardData() {
        return ResponseEntity.ok(Map.of(
                "systemSummary", metricsService.getSystemPerformanceSummary(),
                "frameworkComparison", metricsService.getFrameworkComparison(),
                "topPerformers", metricsService.getTopPerformingFrameworks(),
                "reliabilityDistribution", metricsService.getReliabilityDistribution()
        ));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String,Object>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status","UP",
                "service","MetricsController"
        ));
    }
}
