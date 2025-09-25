package com.agentframework.multi_agent_reliability.controller;

import com.agentframework.multi_agent_reliability.dto.AgentExecutionRequest;
import com.agentframework.multi_agent_reliability.dto.AgentExecutionResponse;
import com.agentframework.multi_agent_reliability.model.AgentFrameworkType;
import com.agentframework.multi_agent_reliability.service.AgentOrchestrationService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/executions")
@CrossOrigin(origins = "*")
public class AgentExecutionController {

    private static final Logger logger = LoggerFactory.getLogger(AgentExecutionController.class);
    private final AgentOrchestrationService orchestrationService;

    public AgentExecutionController(AgentOrchestrationService orchestrationService) {
        this.orchestrationService = orchestrationService;
    }

    @PostMapping("/execute")
    public ResponseEntity<AgentExecutionResponse> executeTask(@Valid @RequestBody AgentExecutionRequest request) {
        try {
            logger.info("Execution request for framework {}", request.getFrameworkType());
            AgentExecutionResponse response;
            if (Boolean.TRUE.equals(request.getAsync())) {
                orchestrationService.executeTaskAsync(request);
                response = new AgentExecutionResponse();
                response.setFrameworkType(request.getFrameworkType());
                response.setTaskDescription("Task queued for async execution");
            } else {
                response = orchestrationService.executeTask(request);
            }
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            logger.error("Invalid exec request: {}", ex.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception ex) {
            logger.error("Execution failed", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/execute/compare")
    public ResponseEntity<Map<String,Object>> executeTaskComparison(
            @Valid @RequestBody AgentExecutionRequest baseRequest,
            @RequestParam List<AgentFrameworkType> frameworks) {
        try {
            logger.info("Comparison across {} frameworks", frameworks.size());
            orchestrationService.executeTaskAcrossFrameworks(baseRequest, frameworks);
            return ResponseEntity.accepted().body(Map.of(
                    "message","Comparison started",
                    "frameworks",frameworks,
                    "status","running"
            ));
        } catch (Exception ex) {
            logger.error("Comparison failed", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{executionId}")
    public ResponseEntity<AgentExecutionResponse> getExecutionStatus(@PathVariable Long executionId) {
        AgentExecutionResponse resp = orchestrationService.getExecutionStatus(executionId);
        return (resp == null)
                ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(resp);
    }

    @GetMapping("/recent")
    public ResponseEntity<List<AgentExecutionResponse>> getRecentExecutions() {
        return ResponseEntity.ok(orchestrationService.getRecentExecutions());
    }

    @GetMapping("/framework/{frameworkType}")
    public ResponseEntity<List<AgentExecutionResponse>> getExecutionsByFramework(
            @PathVariable AgentFrameworkType frameworkType) {
        return ResponseEntity.ok(orchestrationService.getExecutionsByFramework(frameworkType));
    }

    @GetMapping("/benchmark/{runId}")
    public ResponseEntity<List<AgentExecutionResponse>> getExecutionsByBenchmarkRun(
            @PathVariable String runId) {
        return ResponseEntity.ok(orchestrationService.getExecutionsByBenchmarkRun(runId));
    }

    @PostMapping("/{executionId}/cancel")
    public ResponseEntity<Map<String,Object>> cancelExecution(@PathVariable Long executionId) {
        boolean cancelled = orchestrationService.cancelExecution(executionId);
        return ResponseEntity.ok(Map.of(
                "executionId", executionId,
                "cancelled", cancelled
        ));
    }

    @GetMapping("/frameworks")
    public ResponseEntity<Map<AgentFrameworkType,Map<String,Object>>> getAvailableFrameworks() {
        return ResponseEntity.ok(orchestrationService.getAvailableFrameworks());
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<AgentFrameworkType,Map<String,Object>>> getExecutionStatistics() {
        return ResponseEntity.ok(orchestrationService.getExecutionStatistics());
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String,Object>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status","UP",
                "service","AgentExecutionController"
        ));
    }
}
