package com.agentframework.multi_agent_reliability.controller;

import com.agentframework.multi_agent_reliability.dto.BenchmarkRequest;
import com.agentframework.multi_agent_reliability.model.BenchmarkRun;
import com.agentframework.multi_agent_reliability.model.BenchmarkTask;
import com.agentframework.multi_agent_reliability.model.TaskComplexity;
import com.agentframework.multi_agent_reliability.service.BenchmarkService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/benchmarks")
@CrossOrigin(origins = "*")
public class BenchmarkController {

    private static final Logger logger = LoggerFactory.getLogger(BenchmarkController.class);
    private final BenchmarkService benchmarkService;

    public BenchmarkController(BenchmarkService benchmarkService) {
        this.benchmarkService = benchmarkService;
    }

    @PostMapping("/tasks")
    public ResponseEntity<BenchmarkTask> createBenchmarkTask(
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam String taskInput,
            @RequestParam com.agentframework.multi_agent_reliability.model.BenchmarkTask.TaskComplexity complexity,
            @RequestParam(required = false) String expectedOutput,
            @RequestParam(defaultValue = "system") String createdBy) {

        try {
            BenchmarkTask task = benchmarkService.createBenchmarkTask(
                    name, description, taskInput, complexity, expectedOutput, createdBy);
            return ResponseEntity.status(HttpStatus.CREATED).body(task);
        } catch (IllegalArgumentException ex) {
            logger.error("Invalid task params: {}", ex.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception ex) {
            logger.error("Task creation failed", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/execute")
    public ResponseEntity<Map<String,Object>> executeBenchmark(@Valid @RequestBody BenchmarkRequest request) {
        try {
            CompletableFuture<BenchmarkRun> future = benchmarkService.executeBenchmark(request);
            return ResponseEntity.accepted().body(Map.of(
                    "message","Benchmark started",
                    "benchmarkName",request.getName(),
                    "status","running"
            ));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error",ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error","Benchmark failed"));
        }
    }

    @GetMapping("/tasks")
    public ResponseEntity<List<BenchmarkTask>> getAllBenchmarkTasks() {
        return ResponseEntity.ok(benchmarkService.getAllBenchmarkTasks());
    }

    @GetMapping("/tasks/active")
    public ResponseEntity<List<BenchmarkTask>> getActiveBenchmarkTasks() {
        return ResponseEntity.ok(benchmarkService.getActiveBenchmarkTasks());
    }

    @GetMapping("/tasks/{taskId}")
    public ResponseEntity<BenchmarkTask> getBenchmarkTask(@PathVariable Long taskId) {
        Optional<BenchmarkTask> task = benchmarkService.getBenchmarkTask(taskId);
        return task.map(ResponseEntity::ok)
                   .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/tasks/complexity/{complexity}")
    public ResponseEntity<List<BenchmarkTask>> getBenchmarkTasksByComplexity(
            @PathVariable TaskComplexity complexity) {
        return ResponseEntity.ok(benchmarkService.getBenchmarkTasksByComplexity(complexity));
    }

    @PutMapping("/tasks/{taskId}")
    public ResponseEntity<BenchmarkTask> updateBenchmarkTask(
            @PathVariable Long taskId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) com.agentframework.multi_agent_reliability.model.BenchmarkTask.TaskComplexity complexity,
            @RequestParam(required = false) Boolean active) {
        try {
            BenchmarkTask updated = benchmarkService.updateBenchmarkTask(
                    taskId, name, description, complexity, active);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/tasks/{taskId}")
    public ResponseEntity<Map<String,Object>> deleteBenchmarkTask(@PathVariable Long taskId) {
        boolean deleted = benchmarkService.deleteBenchmarkTask(taskId);
        return deleted
                ? ResponseEntity.ok(Map.of("taskId",taskId,"deleted",true))
                : ResponseEntity.notFound().build();
    }

    @GetMapping("/runs/active")
    public ResponseEntity<Map<String,BenchmarkRun>> getActiveBenchmarkRuns() {
        return ResponseEntity.ok(benchmarkService.getActiveBenchmarkRuns());
    }

    @GetMapping("/runs/{runId}")
    public ResponseEntity<BenchmarkRun> getBenchmarkRunStatus(@PathVariable String runId) {
        Optional<BenchmarkRun> run = benchmarkService.getBenchmarkRunStatus(runId);
        return run.map(ResponseEntity::ok)
                  .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/runs/{runId}/cancel")
    public ResponseEntity<Map<String,Object>> cancelBenchmark(@PathVariable String runId) {
        boolean cancelled = benchmarkService.cancelBenchmark(runId);
        return ResponseEntity.ok(Map.of("runId", runId, "cancelled", cancelled));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String,Object>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status","UP",
                "service","BenchmarkController"
        ));
    }
}
