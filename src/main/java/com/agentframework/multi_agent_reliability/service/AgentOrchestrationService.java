package com.agentframework.multi_agent_reliability.service;

import com.agentframework.multi_agent_reliability.adapter.AgentAdapter;
import com.agentframework.multi_agent_reliability.dto.AgentExecutionRequest;
import com.agentframework.multi_agent_reliability.dto.AgentExecutionResponse;
import com.agentframework.multi_agent_reliability.model.AgentExecution;
import com.agentframework.multi_agent_reliability.model.AgentFrameworkType;
import com.agentframework.multi_agent_reliability.model.ExecutionStatus;
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
public class AgentOrchestrationService {
    
    private static final Logger logger = LoggerFactory.getLogger(AgentOrchestrationService.class);
    
    private final AgentExecutionRepository executionRepository;
    private final WebSocketNotificationService notificationService;
    private final Map<AgentFrameworkType, AgentAdapter> adapters;
    
    @Autowired
    public AgentOrchestrationService(
            AgentExecutionRepository executionRepository,
            WebSocketNotificationService notificationService,
            List<AgentAdapter> adapterList) {
        this.executionRepository = executionRepository;
        this.notificationService = notificationService;
        
        // Create adapter map for quick lookup
        this.adapters = adapterList.stream()
                .collect(Collectors.toMap(AgentAdapter::getFrameworkType, adapter -> adapter));
        
        logger.info("Initialized AgentOrchestrationService with {} adapters", adapters.size());
    }
    
    /**
     * Execute a single agent task synchronously
     */
    public AgentExecutionResponse executeTask(AgentExecutionRequest request) {
        logger.info("Executing task synchronously for framework: {}", request.getFrameworkType());
        
        // Create and save initial execution record
        AgentExecution execution = createExecutionRecord(request);
        execution = executionRepository.save(execution);
        
        try {
            // Get the appropriate adapter
            AgentAdapter adapter = getAdapter(request.getFrameworkType());
            if (adapter == null || !adapter.isAvailable()) {
                execution.markAsFailed("Adapter not available for framework: " + request.getFrameworkType());
                execution = executionRepository.save(execution);
                return mapToResponse(execution);
            }
            
            // Mark as started and save
            execution.markAsStarted();
            execution = executionRepository.save(execution);
            notificationService.sendExecutionUpdate(execution);
            
            // Prepare metadata
            Map<String, String> metadata = adapter.prepareMetadata(request.getMetadata());
            
            // Execute the task
            String result = adapter.executeTask(
                request.getTaskInput(),
                request.getTaskDescription(),
                metadata
            );
            
            // Mark as completed
            execution.markAsCompleted(result);
            execution.setMetadata(metadata);
            execution = executionRepository.save(execution);
            
            logger.info("Task completed successfully for execution ID: {}", execution.getId());
            notificationService.sendExecutionUpdate(execution);
            
            return mapToResponse(execution);
            
        } catch (Exception e) {
            logger.error("Task execution failed for execution ID: {}", execution.getId(), e);
            execution.markAsFailed(e.getMessage());
            execution = executionRepository.save(execution);
            notificationService.sendExecutionUpdate(execution);
            
            return mapToResponse(execution);
        }
    }
    
    /**
     * Execute a single agent task asynchronously
     */
    @Async("agentExecutionTaskExecutor")
    public CompletableFuture<AgentExecutionResponse> executeTaskAsync(AgentExecutionRequest request) {
        logger.info("Executing task asynchronously for framework: {}", request.getFrameworkType());
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                return executeTask(request);
            } catch (Exception e) {
                logger.error("Async task execution failed", e);
                throw new RuntimeException(e);
            }
        });
    }
    
    /**
     * Execute the same task across multiple frameworks for comparison
     */
    public List<CompletableFuture<AgentExecutionResponse>> executeTaskAcrossFrameworks(
            AgentExecutionRequest baseRequest, 
            List<AgentFrameworkType> frameworks) {
        
        logger.info("Executing task across {} frameworks", frameworks.size());
        
        List<CompletableFuture<AgentExecutionResponse>> futures = new ArrayList<>();
        
        for (AgentFrameworkType frameworkType : frameworks) {
            // Create a copy of the request for each framework
            AgentExecutionRequest request = copyRequest(baseRequest);
            request.setFrameworkType(frameworkType);
            
            CompletableFuture<AgentExecutionResponse> future = executeTaskAsync(request);
            futures.add(future);
        }
        
        return futures;
    }
    
    /**
     * Get execution status by ID
     */
    public AgentExecutionResponse getExecutionStatus(Long executionId) {
        Optional<AgentExecution> execution = executionRepository.findById(executionId);
        return execution.map(this::mapToResponse).orElse(null);
    }
    
    /**
     * Get recent executions
     */
    public List<AgentExecutionResponse> getRecentExecutions() {
        List<AgentExecution> executions = executionRepository.findTop10ByOrderByCreatedAtDesc();
        return executions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get executions by framework type
     */
    public List<AgentExecutionResponse> getExecutionsByFramework(AgentFrameworkType frameworkType) {
        List<AgentExecution> executions = executionRepository.findByFrameworkType(frameworkType);
        return executions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get executions by benchmark run ID
     */
    public List<AgentExecutionResponse> getExecutionsByBenchmarkRun(String benchmarkRunId) {
        List<AgentExecution> executions = executionRepository.findByBenchmarkRunId(benchmarkRunId);
        return executions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Cancel a pending execution
     */
    public boolean cancelExecution(Long executionId) {
        Optional<AgentExecution> optional = executionRepository.findById(executionId);
        
        if (optional.isPresent()) {
            AgentExecution execution = optional.get();
            if (execution.getStatus() == ExecutionStatus.PENDING || execution.getStatus() == ExecutionStatus.RUNNING) {
                execution.setStatus(ExecutionStatus.CANCELLED);
                execution.setEndTime(LocalDateTime.now());
                executionRepository.save(execution);
                notificationService.sendExecutionUpdate(execution);
                
                logger.info("Execution {} cancelled", executionId);
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Get available frameworks with their configuration
     */
    public Map<AgentFrameworkType, Map<String, Object>> getAvailableFrameworks() {
        Map<AgentFrameworkType, Map<String, Object>> frameworkInfo = new HashMap<>();
        
        for (Map.Entry<AgentFrameworkType, AgentAdapter> entry : adapters.entrySet()) {
            AgentAdapter adapter = entry.getValue();
            Map<String, Object> config = adapter.getConfiguration();
            frameworkInfo.put(entry.getKey(), config);
        }
        
        return frameworkInfo;
    }
    
    /**
     * Get execution statistics by framework
     */
    public Map<AgentFrameworkType, Map<String, Object>> getExecutionStatistics() {
        Map<AgentFrameworkType, Map<String, Object>> stats = new HashMap<>();
        
        for (AgentFrameworkType framework : AgentFrameworkType.values()) {
            Map<String, Object> frameworkStats = new HashMap<>();
            
            List<AgentExecution> executions = executionRepository.findByFrameworkType(framework);
            
            long total = executions.size();
            long successful = executions.stream()
                    .mapToLong(e -> e.getStatus() == ExecutionStatus.COMPLETED ? 1 : 0)
                    .sum();
            long failed = executions.stream()
                    .mapToLong(e -> e.getStatus() == ExecutionStatus.FAILED ? 1 : 0)
                    .sum();
            
            double avgDuration = executions.stream()
                    .filter(e -> e.getExecutionDurationMs() != null)
                    .mapToLong(AgentExecution::getExecutionDurationMs)
                    .average()
                    .orElse(0.0);
            
            double successRate = total > 0 ? (double) successful / total * 100 : 0.0;
            
            frameworkStats.put("total", total);
            frameworkStats.put("successful", successful);
            frameworkStats.put("failed", failed);
            frameworkStats.put("successRate", Math.round(successRate * 100.0) / 100.0);
            frameworkStats.put("averageDurationMs", Math.round(avgDuration));
            
            stats.put(framework, frameworkStats);
        }
        
        return stats;
    }
    
    private AgentAdapter getAdapter(AgentFrameworkType frameworkType) {
        return adapters.get(frameworkType);
    }
    
    private AgentExecution createExecutionRecord(AgentExecutionRequest request) {
        AgentExecution execution = new AgentExecution();
        execution.setFrameworkType(request.getFrameworkType());
        execution.setTaskDescription(request.getTaskDescription());
        execution.setTaskInput(request.getTaskInput());
        execution.setBenchmarkRunId(request.getBenchmarkRunId());
        execution.setStatus(ExecutionStatus.PENDING);
        execution.setMetadata(request.getMetadata());
        return execution;
    }
    
    private AgentExecutionResponse mapToResponse(AgentExecution execution) {
        AgentExecutionResponse response = new AgentExecutionResponse();
        response.setId(execution.getId());
        response.setFrameworkType(execution.getFrameworkType());
        response.setTaskDescription(execution.getTaskDescription());
        response.setTaskInput(execution.getTaskInput());
        response.setTaskOutput(execution.getTaskOutput());
        response.setStatus(execution.getStatus());
        response.setErrorMessage(execution.getErrorMessage());
        response.setExecutionDurationMs(execution.getExecutionDurationMs());
        response.setBenchmarkRunId(execution.getBenchmarkRunId());
        response.setMetadata(execution.getMetadata());
        response.setStartTime(execution.getStartTime());
        response.setEndTime(execution.getEndTime());
        response.setCreatedAt(execution.getCreatedAt());
        return response;
    }
    
    private AgentExecutionRequest copyRequest(AgentExecutionRequest original) {
        AgentExecutionRequest copy = new AgentExecutionRequest();
        copy.setFrameworkType(original.getFrameworkType());
        copy.setTaskDescription(original.getTaskDescription());
        copy.setTaskInput(original.getTaskInput());
        copy.setExpectedOutput(original.getExpectedOutput());
        copy.setTimeoutMs(original.getTimeoutMs());
        copy.setMaxRetries(original.getMaxRetries());
        copy.setBenchmarkRunId(original.getBenchmarkRunId());
        copy.setAsync(original.getAsync());
        
        if (original.getMetadata() != null) {
            copy.setMetadata(new HashMap<>(original.getMetadata()));
        }
        
        return copy;
    }
}