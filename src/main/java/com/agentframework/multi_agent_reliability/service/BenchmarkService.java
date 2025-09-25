package com.agentframework.multi_agent_reliability.service;

import com.agentframework.multi_agent_reliability.dto.BenchmarkRequest;
import com.agentframework.multi_agent_reliability.dto.AgentExecutionRequest;
import com.agentframework.multi_agent_reliability.dto.AgentExecutionResponse;
import com.agentframework.multi_agent_reliability.model.*;
import com.agentframework.multi_agent_reliability.model.BenchmarkRun.BenchmarkRunStatus;
import com.agentframework.multi_agent_reliability.repository.BenchmarkTaskRepository;
import com.agentframework.multi_agent_reliability.repository.AgentExecutionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class BenchmarkService {
    
    private static final Logger logger = LoggerFactory.getLogger(BenchmarkService.class);
    
    private final BenchmarkTaskRepository taskRepository;
    private final AgentExecutionRepository executionRepository;
    private final AgentOrchestrationService orchestrationService;
    private final MetricsCollectionService metricsService;
    private final WebSocketNotificationService notificationService;
    
    // In-memory storage for active benchmark runs
    private final Map<String, BenchmarkRun> activeBenchmarkRuns = new HashMap<>();
    
    @Autowired
    public BenchmarkService(
            BenchmarkTaskRepository taskRepository,
            AgentExecutionRepository executionRepository,
            AgentOrchestrationService orchestrationService,
            MetricsCollectionService metricsService,
            WebSocketNotificationService notificationService) {
        this.taskRepository = taskRepository;
        this.executionRepository = executionRepository;
        this.orchestrationService = orchestrationService;
        this.metricsService = metricsService;
        this.notificationService = notificationService;
    }
    
    /**
     * Create a new benchmark task
     */
    public BenchmarkTask createBenchmarkTask(String name, String description, String taskInput, 
                                           com.agentframework.multi_agent_reliability.model.BenchmarkTask.TaskComplexity complexity, String expectedOutput, String createdBy) {
        
        // Check if task name already exists
        if (taskRepository.existsByNameIgnoreCase(name)) {
            throw new IllegalArgumentException("Benchmark task with name '" + name + "' already exists");
        }
        
        BenchmarkTask task = new BenchmarkTask();
        task.setName(name);
        task.setDescription(description);
        task.setTaskInput(taskInput);
        task.setComplexity(complexity);
        task.setExpectedOutput(expectedOutput);
        task.setCreatedBy(createdBy);
        task.setActive(true);
        
        BenchmarkTask savedTask = taskRepository.save(task);
        logger.info("Created benchmark task: {} (ID: {})", name, savedTask.getId());
        
        return savedTask;
    }
    
    /**
     * Execute a benchmark run across multiple frameworks
     */
    @Async("benchmarkTaskExecutor")
    public CompletableFuture<BenchmarkRun> executeBenchmark(BenchmarkRequest request) {
        logger.info("Starting benchmark execution: {}", request.getName());
        
        // Validate task exists
        Optional<BenchmarkTask> taskOpt = taskRepository.findById(request.getTaskId());
        if (taskOpt.isEmpty()) {
            throw new IllegalArgumentException("Benchmark task not found: " + request.getTaskId());
        }
        
        BenchmarkTask task = taskOpt.get();
        
        // Create benchmark run
        BenchmarkRun benchmarkRun = new BenchmarkRun(task, request.getName());
        benchmarkRun.setDescription(request.getDescription());
        benchmarkRun.setCreatedBy(request.getCreatedBy());
        benchmarkRun.setStatus(BenchmarkRunStatus.RUNNING);
        benchmarkRun.setStartTime(LocalDateTime.now());
        
        // Store in active runs
        activeBenchmarkRuns.put(benchmarkRun.getRunId(), benchmarkRun);
        
        // Send initial notification
        notificationService.sendBenchmarkUpdate(benchmarkRun);
        
        try {
            // Execute benchmark
            CompletableFuture<BenchmarkRun> result = executeFrameworkComparison(benchmarkRun, task, request);
            return result;
            
        } catch (Exception e) {
            logger.error("Benchmark execution failed: {}", request.getName(), e);
            benchmarkRun.setStatus(BenchmarkRunStatus.FAILED);
            benchmarkRun.setEndTime(LocalDateTime.now());
            notificationService.sendBenchmarkUpdate(benchmarkRun);
            throw new RuntimeException("Benchmark execution failed", e);
        }
    }
    
    /**
     * Execute comparison across multiple frameworks
     */
    private CompletableFuture<BenchmarkRun> executeFrameworkComparison(
            BenchmarkRun benchmarkRun, BenchmarkTask task, BenchmarkRequest request) {
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("Executing framework comparison for benchmark: {}", benchmarkRun.getRunId());
                
                List<CompletableFuture<AgentExecutionResponse>> allFutures = new ArrayList<>();
                
                // Create execution requests for each framework and iteration
                for (AgentFrameworkType frameworkType : request.getFrameworkTypes()) {
                    for (int iteration = 1; iteration <= request.getIterations(); iteration++) {
                        
                        AgentExecutionRequest execRequest = new AgentExecutionRequest();
                        execRequest.setFrameworkType(frameworkType);
                        execRequest.setTaskDescription(task.getDescription());
                        execRequest.setTaskInput(task.getTaskInput());
                        execRequest.setExpectedOutput(task.getExpectedOutput());
                        execRequest.setTimeoutMs(request.getTimeoutMs());
                        execRequest.setMaxRetries(request.getMaxRetries());
                        execRequest.setBenchmarkRunId(benchmarkRun.getRunId());
                        
                        // Add iteration metadata
                        Map<String, String> metadata = new HashMap<>();
                        if (request.getMetadata() != null) {
                            metadata.putAll(request.getMetadata());
                        }
                        metadata.put("iteration", String.valueOf(iteration));
                        metadata.put("totalIterations", String.valueOf(request.getIterations()));
                        metadata.put("benchmarkName", request.getName());
                        execRequest.setMetadata(metadata);
                        
                        // Execute asynchronously
                        CompletableFuture<AgentExecutionResponse> future = orchestrationService.executeTaskAsync(execRequest);
                        allFutures.add(future);
                    }
                }
                
                benchmarkRun.setTotalExecutions(allFutures.size());
                
                // Send progress update
                notificationService.sendProgressUpdate(benchmarkRun.getRunId(), "BENCHMARK_EXECUTION", 
                                                     0, allFutures.size(), "Starting executions");
                
                // Wait for all executions to complete with progress tracking
                List<AgentExecutionResponse> completedExecutions = new ArrayList<>();
                int completedCount = 0;
                
                for (CompletableFuture<AgentExecutionResponse> future : allFutures) {
                    try {
                        AgentExecutionResponse execution = future.get();
                        completedExecutions.add(execution);
                        completedCount++;
                        
                        // Update benchmark run statistics
                        updateBenchmarkRunStats(benchmarkRun);
                        
                        // Send progress update
                        notificationService.sendProgressUpdate(benchmarkRun.getRunId(), "BENCHMARK_EXECUTION",
                                                             completedCount, allFutures.size(),
                                                             "Completed execution " + completedCount);
                        
                        // Send benchmark update
                        notificationService.sendBenchmarkUpdate(benchmarkRun);
                        
                    } catch (Exception e) {
                        logger.error("Execution failed in benchmark {}", benchmarkRun.getRunId(), e);
                        completedCount++;
                    }
                }
                
                // Finalize benchmark run
                benchmarkRun.setStatus(BenchmarkRunStatus.COMPLETED);
                benchmarkRun.setEndTime(LocalDateTime.now());
                
                // Calculate and save metrics for each framework
                calculateBenchmarkMetrics(benchmarkRun, request.getFrameworkTypes());
                
                // Send final update
                notificationService.sendBenchmarkUpdate(benchmarkRun);
                
                logger.info("Benchmark execution completed: {} ({} executions)", 
                           benchmarkRun.getRunId(), completedExecutions.size());
                
                return benchmarkRun;
                
            } catch (Exception e) {
                logger.error("Framework comparison failed for benchmark {}", benchmarkRun.getRunId(), e);
                benchmarkRun.setStatus(BenchmarkRunStatus.FAILED);
                benchmarkRun.setEndTime(LocalDateTime.now());
                notificationService.sendBenchmarkUpdate(benchmarkRun);
                throw new RuntimeException("Framework comparison failed", e);
            } finally {
                // Remove from active runs
                activeBenchmarkRuns.remove(benchmarkRun.getRunId());
            }
        });
    }
    
    /**
     * Get all benchmark tasks
     */
    public List<BenchmarkTask> getAllBenchmarkTasks() {
        return taskRepository.findAllByOrderByCreatedAtDesc();
    }
    
    /**
     * Get active benchmark tasks
     */
    public List<BenchmarkTask> getActiveBenchmarkTasks() {
        return taskRepository.findByActiveTrue();
    }
    
    /**
     * Get benchmark task by ID
     */
    public Optional<BenchmarkTask> getBenchmarkTask(Long taskId) {
        return taskRepository.findById(taskId);
    }
    
    /**
     * Get benchmark tasks by complexity
     */
    public List<BenchmarkTask> getBenchmarkTasksByComplexity(TaskComplexity complexity) {
        return taskRepository.findByActiveTrueAndComplexity(complexity);
    }
    
    /**
     * Get active benchmark runs
     */
    public Map<String, BenchmarkRun> getActiveBenchmarkRuns() {
        return new HashMap<>(activeBenchmarkRuns);
    }
    
    /**
     * Get benchmark run status
     */
    public Optional<BenchmarkRun> getBenchmarkRunStatus(String runId) {
        return Optional.ofNullable(activeBenchmarkRuns.get(runId));
    }
    
    /**
     * Cancel a running benchmark
     */
    public boolean cancelBenchmark(String runId) {
        BenchmarkRun run = activeBenchmarkRuns.get(runId);
        if (run != null && !run.getStatus().isTerminal()) {
            run.setStatus(BenchmarkRunStatus.CANCELLED);
            run.setEndTime(LocalDateTime.now());
            notificationService.sendBenchmarkUpdate(run);
            activeBenchmarkRuns.remove(runId);
            
            logger.info("Benchmark cancelled: {}", runId);
            return true;
        }
        return false;
    }
    
    /**
     * Update benchmark task
     */
    public BenchmarkTask updateBenchmarkTask(Long taskId, String name, String description, 
                                           com.agentframework.multi_agent_reliability.model.BenchmarkTask.TaskComplexity complexity, Boolean active) {
        Optional<BenchmarkTask> taskOpt = taskRepository.findById(taskId);
        if (taskOpt.isEmpty()) {
            throw new IllegalArgumentException("Benchmark task not found: " + taskId);
        }
        
        BenchmarkTask task = taskOpt.get();
        if (name != null) task.setName(name);
        if (description != null) task.setDescription(description);
        if (complexity != null) task.setComplexity(complexity);
        if (active != null) task.setActive(active);
        
        return taskRepository.save(task);
    }
    
    /**
     * Delete benchmark task
     */
    public boolean deleteBenchmarkTask(Long taskId) {
        if (taskRepository.existsById(taskId)) {
            taskRepository.deleteById(taskId);
            logger.info("Deleted benchmark task: {}", taskId);
            return true;
        }
        return false;
    }
    
    private void updateBenchmarkRunStats(BenchmarkRun benchmarkRun) {
        List<AgentExecution> executions = executionRepository.findByBenchmarkRunId(benchmarkRun.getRunId());
        
        int completed = 0;
        int failed = 0;
        
        for (AgentExecution execution : executions) {
            if (execution.getStatus() == ExecutionStatus.COMPLETED) {
                completed++;
            } else if (execution.getStatus() == ExecutionStatus.FAILED) {
                failed++;
            }
        }
        
        benchmarkRun.setCompletedExecutions(completed);
        benchmarkRun.setFailedExecutions(failed);
    }
    
    private void calculateBenchmarkMetrics(BenchmarkRun benchmarkRun, List<AgentFrameworkType> frameworks) {
        logger.info("Calculating metrics for benchmark: {}", benchmarkRun.getRunId());
        
        for (AgentFrameworkType framework : frameworks) {
            try {
                metricsService.calculateFrameworkMetrics(benchmarkRun.getRunId(), framework);
            } catch (Exception e) {
                logger.error("Failed to calculate metrics for framework {} in benchmark {}", 
                           framework, benchmarkRun.getRunId(), e);
            }
        }
    }
}