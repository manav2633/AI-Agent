package com.agentframework.multi_agent_reliability.service;
import com.agentframework.multi_agent_reliability.model.AgentExecution;
import com.agentframework.multi_agent_reliability.model.BenchmarkRun;
import com.agentframework.multi_agent_reliability.model.ReliabilityMetrics;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class WebSocketNotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(WebSocketNotificationService.class);
    
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;
    
    // WebSocket topic constants
    private static final String EXECUTION_UPDATES = "/topic/executions";
    private static final String BENCHMARK_UPDATES = "/topic/benchmarks";
    private static final String METRICS_UPDATES = "/topic/metrics";
    private static final String SYSTEM_UPDATES = "/topic/system";
    
    @Autowired
    public WebSocketNotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Send execution status update
     */
    public void sendExecutionUpdate(AgentExecution execution) {
        try {
            Map<String, Object> update = new HashMap<>();
            update.put("type", "EXECUTION_UPDATE");
            update.put("timestamp", LocalDateTime.now());
            update.put("executionId", execution.getId());
            update.put("frameworkType", execution.getFrameworkType());
            update.put("status", execution.getStatus());
            update.put("duration", execution.getExecutionDurationMs());
            update.put("benchmarkRunId", execution.getBenchmarkRunId());
            
            if (execution.getStatus().isTerminal()) {
                update.put("completed", true);
                update.put("successful", execution.getStatus().isSuccess());
                if (execution.getErrorMessage() != null) {
                    update.put("error", execution.getErrorMessage());
                }
            }
            
            messagingTemplate.convertAndSend(EXECUTION_UPDATES, update);
            logger.debug("Sent execution update for execution {}: {}", execution.getId(), execution.getStatus());
            
        } catch (Exception e) {
            logger.error("Failed to send execution update for execution {}", execution.getId(), e);
        }
    }
    
    /**
     * Send benchmark run status update
     */
    public void sendBenchmarkUpdate(BenchmarkRun benchmarkRun) {
        try {
            Map<String, Object> update = new HashMap<>();
            update.put("type", "BENCHMARK_UPDATE");
            update.put("timestamp", LocalDateTime.now());
            update.put("benchmarkRunId", benchmarkRun.getRunId());
            update.put("name", benchmarkRun.getName());
            update.put("status", benchmarkRun.getStatus());
            update.put("totalExecutions", benchmarkRun.getTotalExecutions());
            update.put("completedExecutions", benchmarkRun.getCompletedExecutions());
            update.put("failedExecutions", benchmarkRun.getFailedExecutions());
            update.put("successRate", benchmarkRun.getSuccessRate());
            
            if (benchmarkRun.getStatus().isTerminal()) {
                update.put("completed", true);
                if (benchmarkRun.getStartTime() != null && benchmarkRun.getEndTime() != null) {
                    long duration = java.time.Duration.between(benchmarkRun.getStartTime(), benchmarkRun.getEndTime()).toMillis();
                    update.put("totalDurationMs", duration);
                }
            }
            
            messagingTemplate.convertAndSend(BENCHMARK_UPDATES, update);
            logger.debug("Sent benchmark update for run {}: {}", benchmarkRun.getRunId(), benchmarkRun.getStatus());
            
        } catch (Exception e) {
            logger.error("Failed to send benchmark update for run {}", benchmarkRun.getRunId(), e);
        }
    }
    
    /**
     * Send metrics calculation update
     */
    public void sendMetricsUpdate(ReliabilityMetrics metrics) {
        try {
            Map<String, Object> update = new HashMap<>();
            update.put("type", "METRICS_UPDATE");
            update.put("timestamp", LocalDateTime.now());
            update.put("metricsId", metrics.getId());
            update.put("benchmarkRunId", metrics.getBenchmarkRunId());
            update.put("frameworkType", metrics.getFrameworkType());
            update.put("successRate", metrics.getSuccessRate());
            update.put("averageResponseTime", metrics.getAverageResponseTimeMs());
            update.put("consistencyScore", metrics.getConsistencyScore());
            update.put("robustnessIndex", metrics.getRobustnessIndex());
            update.put("overallScore", metrics.getOverallReliabilityScore());
            
            messagingTemplate.convertAndSend(METRICS_UPDATES, update);
            logger.debug("Sent metrics update for framework {} in run {}", 
                        metrics.getFrameworkType(), metrics.getBenchmarkRunId());
            
        } catch (Exception e) {
            logger.error("Failed to send metrics update for metrics {}", metrics.getId(), e);
        }
    }
    
    /**
     * Send system status update
     */
    public void sendSystemUpdate(String component, String status, String message) {
        try {
            Map<String, Object> update = new HashMap<>();
            update.put("type", "SYSTEM_UPDATE");
            update.put("timestamp", LocalDateTime.now());
            update.put("component", component);
            update.put("status", status);
            update.put("message", message);
            
            messagingTemplate.convertAndSend(SYSTEM_UPDATES, update);
            logger.debug("Sent system update for {}: {}", component, status);
            
        } catch (Exception e) {
            logger.error("Failed to send system update for {}", component, e);
        }
    }
    
    /**
     * Send framework availability update
     */
    public void sendFrameworkAvailabilityUpdate(Map<String, Object> availabilityStatus) {
        try {
            Map<String, Object> update = new HashMap<>();
            update.put("type", "FRAMEWORK_AVAILABILITY");
            update.put("timestamp", LocalDateTime.now());
            update.put("frameworks", availabilityStatus);
            
            messagingTemplate.convertAndSend(SYSTEM_UPDATES, update);
            logger.debug("Sent framework availability update");
            
        } catch (Exception e) {
            logger.error("Failed to send framework availability update", e);
        }
    }
    
    /**
     * Send real-time statistics update
     */
    public void sendStatisticsUpdate(Map<String, Object> statistics) {
        try {
            Map<String, Object> update = new HashMap<>();
            update.put("type", "STATISTICS_UPDATE");
            update.put("timestamp", LocalDateTime.now());
            update.put("statistics", statistics);
            
            messagingTemplate.convertAndSend(SYSTEM_UPDATES, update);
            logger.debug("Sent statistics update");
            
        } catch (Exception e) {
            logger.error("Failed to send statistics update", e);
        }
    }
    
    /**
     * Send error notification
     */
    public void sendErrorNotification(String component, String errorMessage, Exception exception) {
        try {
            Map<String, Object> update = new HashMap<>();
            update.put("type", "ERROR_NOTIFICATION");
            update.put("timestamp", LocalDateTime.now());
            update.put("component", component);
            update.put("error", errorMessage);
            update.put("severity", "ERROR");
            
            if (exception != null) {
                update.put("exceptionType", exception.getClass().getSimpleName());
                update.put("exceptionMessage", exception.getMessage());
            }
            
            messagingTemplate.convertAndSend(SYSTEM_UPDATES, update);
            logger.debug("Sent error notification for {}: {}", component, errorMessage);
            
        } catch (Exception e) {
            logger.error("Failed to send error notification for {}", component, e);
        }
    }
    
    /**
     * Send warning notification
     */
    public void sendWarningNotification(String component, String warningMessage) {
        try {
            Map<String, Object> update = new HashMap<>();
            update.put("type", "WARNING_NOTIFICATION");
            update.put("timestamp", LocalDateTime.now());
            update.put("component", component);
            update.put("warning", warningMessage);
            update.put("severity", "WARNING");
            
            messagingTemplate.convertAndSend(SYSTEM_UPDATES, update);
            logger.debug("Sent warning notification for {}: {}", component, warningMessage);
            
        } catch (Exception e) {
            logger.error("Failed to send warning notification for {}", component, e);
        }
    }
    
    /**
     * Send progress update for long-running operations
     */
    public void sendProgressUpdate(String operationId, String operationType, int currentStep, int totalSteps, String currentTask) {
        try {
            Map<String, Object> update = new HashMap<>();
            update.put("type", "PROGRESS_UPDATE");
            update.put("timestamp", LocalDateTime.now());
            update.put("operationId", operationId);
            update.put("operationType", operationType);
            update.put("currentStep", currentStep);
            update.put("totalSteps", totalSteps);
            update.put("currentTask", currentTask);
            update.put("progress", totalSteps > 0 ? (double) currentStep / totalSteps * 100 : 0);
            
            messagingTemplate.convertAndSend(SYSTEM_UPDATES, update);
            logger.debug("Sent progress update for {}: {}/{}", operationId, currentStep, totalSteps);
            
        } catch (Exception e) {
            logger.error("Failed to send progress update for {}", operationId, e);
        }
    }
    
    /**
     * Broadcast a general message to all connected clients
     */
    public void broadcastMessage(String messageType, String message) {
        try {
            Map<String, Object> update = new HashMap<>();
            update.put("type", messageType);
            update.put("timestamp", LocalDateTime.now());
            update.put("message", message);
            
            messagingTemplate.convertAndSend(SYSTEM_UPDATES, update);
            logger.debug("Broadcast message of type {}: {}", messageType, message);
            
        } catch (Exception e) {
            logger.error("Failed to broadcast message of type {}", messageType, e);
        }
    }
}