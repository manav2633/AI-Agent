
package com.agentframework.multi_agent_reliability.dto;

import com.agentframework.multi_agent_reliability.model.AgentFrameworkType;
import com.agentframework.multi_agent_reliability.model.ExecutionStatus;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.Map;

public class AgentExecutionResponse {
    
    private Long id;
    private AgentFrameworkType frameworkType;
    private String taskDescription;
    private String taskInput;
    private String taskOutput;
    private ExecutionStatus status;
    private String errorMessage;
    private Long executionDurationMs;
    private String benchmarkRunId;
    private Map<String, String> metadata;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    // Constructors
    public AgentExecutionResponse() {}
    
    public AgentExecutionResponse(Long id, AgentFrameworkType frameworkType, ExecutionStatus status) {
        this.id = id;
        this.frameworkType = frameworkType;
        this.status = status;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public AgentFrameworkType getFrameworkType() {
        return frameworkType;
    }
    
    public void setFrameworkType(AgentFrameworkType frameworkType) {
        this.frameworkType = frameworkType;
    }
    
    public String getTaskDescription() {
        return taskDescription;
    }
    
    public void setTaskDescription(String taskDescription) {
        this.taskDescription = taskDescription;
    }
    
    public String getTaskInput() {
        return taskInput;
    }
    
    public void setTaskInput(String taskInput) {
        this.taskInput = taskInput;
    }
    
    public String getTaskOutput() {
        return taskOutput;
    }
    
    public void setTaskOutput(String taskOutput) {
        this.taskOutput = taskOutput;
    }
    
    public ExecutionStatus getStatus() {
        return status;
    }
    
    public void setStatus(ExecutionStatus status) {
        this.status = status;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public Long getExecutionDurationMs() {
        return executionDurationMs;
    }
    
    public void setExecutionDurationMs(Long executionDurationMs) {
        this.executionDurationMs = executionDurationMs;
    }
    
    public String getBenchmarkRunId() {
        return benchmarkRunId;
    }
    
    public void setBenchmarkRunId(String benchmarkRunId) {
        this.benchmarkRunId = benchmarkRunId;
    }
    
    public Map<String, String> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }
    
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    // Utility methods
    public String getFormattedDuration() {
        if (executionDurationMs == null) return "N/A";
        
        long seconds = executionDurationMs / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        
        if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds);
        } else {
            return String.format("%ds", seconds);
        }
    }
    
    public boolean isCompleted() {
        return status != null && status.isTerminal();
    }
    
    public boolean isSuccessful() {
        return status != null && status.isSuccess();
    }
    
    @Override
    public String toString() {
        return "AgentExecutionResponse{" +
                "id=" + id +
                ", frameworkType=" + frameworkType +
                ", status=" + status +
                ", duration=" + getFormattedDuration() +
                '}';
    }
}